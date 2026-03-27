package com.finance.saving_planner.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.finance.saving_planner.dto.InsuranceDTO
import com.finance.saving_planner.dto.MonthlyExpensesDTO
import com.finance.saving_planner.dto.PersonalFinanceOverviewDTO
import com.finance.saving_planner.dto.SubscriptionDTO
import com.finance.saving_planner.model.Insurance
import com.finance.saving_planner.model.MonthlyExpenses
import com.finance.saving_planner.model.PersonalFinance
import com.finance.saving_planner.model.Subscription
import com.finance.saving_planner.repository.PersonalFinanceRepository
import com.finance.saving_planner.service.PersonalFinanceService
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID
import kotlin.math.abs

@Service
class PersonalFinanceServiceImpl(private val personalFinanceRepository: PersonalFinanceRepository) : PersonalFinanceService {
    companion object {
        private val logger = LoggerFactory.getLogger(PersonalFinanceServiceImpl::class.java)
        private val csvDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private const val csvBatchSize = 1000
    }

    override fun createPersonalFinanceOverview(personalFinanceDto: PersonalFinanceOverviewDTO): PersonalFinance {
        logger.info("Creating personal finance overview for period {} to {}", personalFinanceDto.startDate, personalFinanceDto.endDate)
        validateDateRange(personalFinanceDto.startDate, personalFinanceDto.endDate)

        val personalFinanceEntity = PersonalFinance(
            startDate = personalFinanceDto.startDate,
            endDate = personalFinanceDto.endDate,
            monthlyIncome = personalFinanceDto.monthlyIncome,
            monthlyExpenses = createMonthlyExpenses(personalFinanceDto),
            consumption = personalFinanceDto.consumption,
            savings = personalFinanceDto.savings,
            investments = personalFinanceDto.investments,
        )
        val savedPersonalFinance = personalFinanceRepository.save(personalFinanceEntity)
        logger.info("Created personal finance overview with id {}", savedPersonalFinance.id)
        return savedPersonalFinance
    }

    @Transactional(readOnly = true)
    override fun getTotalOverview(): Collection<PersonalFinanceOverviewDTO> {
        logger.debug("Fetching all personal finance overviews")
        return personalFinanceRepository.findAll().map(::toPersonalFinanceOverviewDto)
    }

    override fun updatePersonalFinanceOverview(financeId: UUID, personalFinance: JsonNode): String {
        val finance = personalFinanceRepository.findById(financeId).orElseThrow {
            IllegalArgumentException("Personal Finance Overview with ID $financeId not found")
        }

        val updatedStartDate = parseDate(personalFinance, "startDate", finance.startDate)
        val updatedEndDate = parseDate(personalFinance, "endDate", finance.endDate)
        validateDateRange(updatedStartDate, updatedEndDate)

        val updatedFinanceOverview = finance.copy(
            startDate = updatedStartDate,
            endDate = updatedEndDate,
            monthlyIncome = personalFinance.readDoubleOrCurrent("monthlyIncome", finance.monthlyIncome),
            monthlyExpenses = personalFinance["monthlyExpenses"]?.let {
                require(it.isObject) { "Field 'monthlyExpenses' must be an object" }
                createMonthlyExpensesFrom(it, finance.monthlyExpenses)
            } ?: finance.monthlyExpenses,
            consumption = personalFinance.readDoubleOrCurrent("consumption", finance.consumption),
            savings = personalFinance.readNullableDouble("savings", finance.savings),
            investments = personalFinance.readNullableDouble("investments", finance.investments),
            updatedAt = LocalDateTime.now(),
        )

        val savedFinanceOverview = personalFinanceRepository.save(updatedFinanceOverview)
        logger.info("Updated personal finance overview with id {}", financeId)
        return "Personal Finance Overview with id ${savedFinanceOverview.id} successfully updated"
    }

    override fun deletePersonalFinanceOverview(financeId: UUID): String {
        logger.info("Deleting personal finance overview with id {}", financeId)
        require(personalFinanceRepository.existsById(financeId)) {
            throw IllegalArgumentException("Personal Finance Overview with ID $financeId not found")
        }

        personalFinanceRepository.deleteById(financeId)
        logger.info("Deleted personal finance overview with id {}", financeId)
        return "Personal Finance Overview with id $financeId successfully deleted"
    }

    @Transactional(readOnly = true)
    override fun getPersonalFinanceOverview(financeId: UUID): PersonalFinanceOverviewDTO {
        logger.info("Fetching personal finance overview with id {}", financeId)
        return personalFinanceRepository.findByFinanceId(financeId)?.let(::toPersonalFinanceOverviewDto)?.also {
            logger.debug("Fetched personal finance overview with id {}", financeId)
        } ?: throw IllegalArgumentException("Personal Finance Overview with ID $financeId not found")
    }

    @Transactional
    override fun processCsv(inputStream: InputStream) {
        val format = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setTrim(true)
            .get()

        BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            format.parse(reader).use { parser ->
                val batch = mutableListOf<PersonalFinance>()

                for (record in parser) {
                    batch.add(createPersonalFinanceFromCsvRecord(record))

                    if (batch.size >= csvBatchSize) {
                        saveBatch(batch)
                    }
                }

                saveBatch(batch)
            }
        }
    }

    private fun parseCsvDate(value: String): Date {
        val localDate = try {
            LocalDate.parse(value.trim(), csvDateFormatter)
        } catch (exception: Exception) {
            throw IllegalArgumentException("Invalid CSV date '$value'. Expected format: dd.MM.yyyy", exception)
        }

        return Date.from(
            localDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        )
    }

    private fun parseDate(personalFinance: JsonNode, fieldName: String, currentValue: Date?): Date? {
        val fieldValue = personalFinance[fieldName] ?: return currentValue

        if (fieldValue.isNull) {
            return null
        }

        return try {
            when {
                fieldValue.isNumber -> Date(fieldValue.asLong())
                else -> Date.from(Instant.parse(fieldValue.asText()))
            }
        } catch (exception: Exception) {
            throw IllegalArgumentException(
                "Invalid date format for field '$fieldName'. Use ISO-8601 or epoch milliseconds",
                exception,
            )
        }
    }

    private fun createMonthlyExpenses(personalFinanceDto: PersonalFinanceOverviewDTO) : MonthlyExpenses {
        logger.info("Creating new monthly expenses with default values")
        val monthlyExpenses = MonthlyExpenses(
            id = personalFinanceDto.monthlyExpenses.id,
            mortgagePayment = personalFinanceDto.monthlyExpenses.mortgagePayment,
            sharedHouseCost = personalFinanceDto.monthlyExpenses.sharedHouseCost,
            foodBudget = personalFinanceDto.monthlyExpenses.foodBudget,
            carLoan = personalFinanceDto.monthlyExpenses.carLoan,
            creditCardBill = personalFinanceDto.monthlyExpenses.creditCardBill,
            electricityBill = personalFinanceDto.monthlyExpenses.electricityBill,
            studentLoans = personalFinanceDto.monthlyExpenses.studentLoans,
            tollFees = personalFinanceDto.monthlyExpenses.tollFees,
            insurances = createInsurance(personalFinanceDto.monthlyExpenses).toMutableList(),
            subscriptions = createSubscriptions(personalFinanceDto.monthlyExpenses).toMutableList(),

        )
        logger.debug("Created new monthly expenses with id {}", monthlyExpenses.id)
        return monthlyExpenses
    }

    private fun createInsurance(monthlyExpensesDto: MonthlyExpensesDTO) : List<Insurance> {
        logger.info("Creating new insurance with default values")
        return monthlyExpensesDto.insurances
            ?.map { insuranceDto ->
                Insurance(
                    id = insuranceDto.id,
                    insuranceType = insuranceDto.insuranceType,
                    insuranceCost = insuranceDto.insuranceCost,
                    insuranceCompany = insuranceDto.insuranceCompany,
                )
            }
            ?: emptyList()
    }

    private fun createSubscriptions(monthlyExpensesDto: MonthlyExpensesDTO) : List<Subscription> {
        logger.info("Creating new subscriptions with default values")
        return monthlyExpensesDto.subscriptions
            ?.map { subscriptionDto ->
                Subscription(
                    id = subscriptionDto.id,
                    subscriptionName = subscriptionDto.subscriptionName,
                    subscriptionCost = subscriptionDto.subscriptionCost,
                )
            }
            ?: emptyList()
    }
    
    private fun createMonthlyExpensesFrom(jsonNode: JsonNode, currentMonthlyExpenses: MonthlyExpenses) : MonthlyExpenses {
        logger.info("Creating monthly expenses from JSON payload")

        val insurances = jsonNode.readArrayOrCurrent("insurances", currentMonthlyExpenses.insurances, ::createInsuranceFrom)

        val subscriptions = jsonNode.readArrayOrCurrent("subscriptions", currentMonthlyExpenses.subscriptions, ::createSubscriptionFrom)

        return MonthlyExpenses(
            id = parseUuid(jsonNode["id"], currentMonthlyExpenses.id),
            mortgagePayment = jsonNode.readDoubleOrCurrent("mortgagePayment", currentMonthlyExpenses.mortgagePayment),
            sharedHouseCost = jsonNode.readDoubleOrCurrent("sharedHouseCost", currentMonthlyExpenses.sharedHouseCost),
            foodBudget = jsonNode.readDoubleOrCurrent("foodBudget", currentMonthlyExpenses.foodBudget),
            carLoan = jsonNode.readNullableDouble("carLoan", currentMonthlyExpenses.carLoan),
            creditCardBill = jsonNode.readNullableDouble("creditCardBill", currentMonthlyExpenses.creditCardBill),
            electricityBill = jsonNode.readNullableDouble("electricityBill", currentMonthlyExpenses.electricityBill),
            studentLoans = jsonNode.readNullableDouble("studentLoans", currentMonthlyExpenses.studentLoans),
            tollFees = jsonNode.readNullableDouble("tollFees", currentMonthlyExpenses.tollFees),
            insurances = insurances,
            subscriptions = subscriptions,
        )
    }

    private fun createInsuranceFrom(jsonNode: JsonNode): Insurance =
        Insurance(
            id = parseUuid(jsonNode["id"]),
            insuranceType = readRequiredText(jsonNode, "insuranceType"),
            insuranceCost = readRequiredDouble(jsonNode, "insuranceCost"),
            insuranceCompany = readRequiredText(jsonNode, "insuranceCompany"),
        )

    private fun createSubscriptionFrom(jsonNode: JsonNode): Subscription =
        Subscription(
            id = parseUuid(jsonNode["id"]),
            subscriptionName = readRequiredText(jsonNode, "subscriptionName"),
            subscriptionCost = readRequiredDouble(jsonNode, "subscriptionCost"),
        )

    private fun parseUuid(node: JsonNode?, fallback: UUID? = UUID.randomUUID()): UUID? {
        if (node == null || node.isNull || node.asText().isBlank()) {
            return fallback
        }

        return try {
            UUID.fromString(node.asText())
        } catch (exception: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid UUID value: ${node.asText()}", exception)
        }
    }

    private fun readRequiredText(jsonNode: JsonNode, fieldName: String): String {
        val value = jsonNode[fieldName]?.asText()?.trim()
        require(!value.isNullOrBlank()) { "Field '$fieldName' is required" }
        return value
    }

    private fun readRequiredDouble(jsonNode: JsonNode, fieldName: String): Double {
        val field = jsonNode[fieldName]
            ?: throw IllegalArgumentException("Field '$fieldName' is required")

        require(field.isNumber) { "Field '$fieldName' must be numeric" }
        return field.asDouble()
    }

    private fun JsonNode.readDoubleOrCurrent(fieldName: String, currentValue: Double): Double {
        val field = this[fieldName] ?: return currentValue
        require(!field.isNull) { "Field '$fieldName' cannot be null" }
        require(field.isNumber) { "Field '$fieldName' must be numeric" }
        return field.asDouble()
    }

    private fun JsonNode.readNullableDouble(fieldName: String, currentValue: Double?): Double? {
        val field = this[fieldName] ?: return currentValue
        if (field.isNull) {
            return null
        }

        require(field.isNumber) { "Field '$fieldName' must be numeric" }
        return field.asDouble()
    }

    private fun <T> JsonNode.readArrayOrCurrent(
        fieldName: String,
        currentValue: List<T>,
        mapper: (JsonNode) -> T,
    ): MutableList<T> {
        val field = this[fieldName] ?: return currentValue.toMutableList()
        require(field.isArray) { "Field '$fieldName' must be an array" }
        return field.map(mapper).toMutableList()
    }

    private fun createPersonalFinanceFromCsvRecord(record: CSVRecord): PersonalFinance {
        val startDate = parseCsvDate(record.getRequiredValue("startDate"))
        val endDate = parseCsvDate(record.getRequiredValue("endDate"))
        validateDateRange(startDate, endDate)

        return PersonalFinance(
            startDate = startDate,
            endDate = endDate,
            monthlyIncome = record.getRequiredDouble("monthlyIncome"),
            monthlyExpenses = MonthlyExpenses(
                mortgagePayment = record.getDoubleOrDefault("mortgagePayment", normalizeToPositive = true),
                sharedHouseCost = record.getDoubleOrDefault("sharedHouseCost", normalizeToPositive = true),
                foodBudget = record.getDoubleOrDefault("foodBudget", normalizeToPositive = true),
                carLoan = record.getNullableDouble("carLoan", normalizeToPositive = true),
                creditCardBill = record.getNullableDouble("creditCardBill", normalizeToPositive = true),
                electricityBill = record.getNullableDouble("electricityBill", normalizeToPositive = true),
                studentLoans = record.getNullableDouble("studentLoans", normalizeToPositive = true),
                tollFees = record.getNullableDouble("tollFees", normalizeToPositive = true),
                subscriptions = mutableListOf(),
                insurances = mutableListOf(),
            ),
            consumption = record.getDoubleOrDefault("consumption", normalizeToPositive = true),
            savings = record.getDoubleOrDefault("savings", normalizeToPositive = true),
            investments = record.getDoubleOrDefault("investments", normalizeToPositive = true),
        )
    }

    private fun CSVRecord.getRequiredValue(columnName: String): String {
        val value = this[columnName].trim()
        require(value.isNotBlank()) { "Field '$columnName' is required in CSV record $recordNumber" }
        return value
    }

    private fun CSVRecord.getRequiredDouble(columnName: String): Double {
        val value = getRequiredValue(columnName)
        return parseCsvNumericValue(value, columnName, normalizeToPositive = false)
    }

    private fun CSVRecord.getDoubleOrDefault(
        columnName: String,
        defaultValue: Double = 0.0,
        normalizeToPositive: Boolean = false,
    ): Double {
        val value = this[columnName].trim()
        if (value.isBlank()) {
            return defaultValue
        }

        return parseCsvNumericValue(value, columnName, normalizeToPositive)
    }

    private fun CSVRecord.getNullableDouble(columnName: String, normalizeToPositive: Boolean = false): Double? {
        val value = this[columnName].trim()
        if (value.isBlank()) {
            return null
        }

        return parseCsvNumericValue(value, columnName, normalizeToPositive)
    }

    private fun CSVRecord.parseCsvNumericValue(value: String, columnName: String, normalizeToPositive: Boolean): Double {
        val normalizedValue = value
            .trim()
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")
            .replace("\u00A0", "")
            .replace("\u202F", "")
            .replace(" ", "")

        val numericValue = when {
            normalizedValue.contains(',') && normalizedValue.contains('.') -> {
                if (normalizedValue.lastIndexOf(',') > normalizedValue.lastIndexOf('.')) {
                    normalizedValue.replace(".", "").replace(',', '.')
                } else {
                    normalizedValue.replace(",", "")
                }
            }
            normalizedValue.contains(',') -> normalizedValue.replace(',', '.')
            else -> normalizedValue
        }.toDoubleOrNull()
            ?: throw IllegalArgumentException("Field '$columnName' must be numeric in CSV record $recordNumber")

        return if (normalizeToPositive) abs(numericValue) else numericValue
    }

    private fun saveBatch(batch: MutableList<PersonalFinance>) {
        if (batch.isEmpty()) {
            return
        }

        personalFinanceRepository.saveAll(batch)
        batch.clear()
    }

    private fun validateDateRange(startDate: Date?, endDate: Date?) {
        if (startDate != null && endDate != null) {
            require(!endDate.before(startDate)) { "endDate must be greater than or equal to startDate" }
        }
    }

    private fun toPersonalFinanceOverviewDto(personalFinance: PersonalFinance): PersonalFinanceOverviewDTO =
        PersonalFinanceOverviewDTO(
            id = personalFinance.id,
            startDate = personalFinance.startDate,
            endDate = personalFinance.endDate,
            monthlyIncome = personalFinance.monthlyIncome,
            monthlyExpenses = toMonthlyExpensesDto(personalFinance.monthlyExpenses),
            consumption = personalFinance.consumption,
            savings = personalFinance.savings,
            investments = personalFinance.investments,
        )

    private fun toMonthlyExpensesDto(monthlyExpenses: MonthlyExpenses): MonthlyExpensesDTO =
        MonthlyExpensesDTO(
            id = monthlyExpenses.id,
            mortgagePayment = monthlyExpenses.mortgagePayment,
            sharedHouseCost = monthlyExpenses.sharedHouseCost,
            foodBudget = monthlyExpenses.foodBudget,
            carLoan = monthlyExpenses.carLoan,
            creditCardBill = monthlyExpenses.creditCardBill,
            electricityBill = monthlyExpenses.electricityBill,
            studentLoans = monthlyExpenses.studentLoans,
            tollFees = monthlyExpenses.tollFees,
            insurances = monthlyExpenses.insurances.map(::toInsuranceDto),
            subscriptions = monthlyExpenses.subscriptions.map(::toSubscriptionDto),
        )

    private fun toInsuranceDto(insurance: Insurance): InsuranceDTO =
        InsuranceDTO(
            id = insurance.id,
            insuranceType = insurance.insuranceType,
            insuranceCost = insurance.insuranceCost,
            insuranceCompany = insurance.insuranceCompany,
        )

    private fun toSubscriptionDto(subscription: Subscription): SubscriptionDTO =
        SubscriptionDTO(
            id = subscription.id,
            subscriptionName = subscription.subscriptionName,
            subscriptionCost = subscription.subscriptionCost,
        )
}