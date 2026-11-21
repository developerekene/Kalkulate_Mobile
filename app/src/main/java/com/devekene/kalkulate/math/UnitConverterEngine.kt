package com.devekene.kalkulate.math

enum class UnitCategory(val title: String) {
  LENGTH("Length"),
  MASS("Mass"),
  TEMPERATURE("Temp"),
  ANGLE("Angle"),
  ENERGY("Energy"),
  SPEED("Speed"),
  DATA("Data"),
}

data class ConversionUnit(
  val id: String,
  val name: String,
  val symbol: String,
  val toBaseFactor: Double = 1.0,
)

data class PhysicalConstant(
  val symbol: String,
  val name: String,
  val value: Double,
  val unit: String,
  val insertToken: String,
)

object UnitConverterEngine {

  val unitsByCategory: Map<UnitCategory, List<ConversionUnit>> =
    mapOf(
      UnitCategory.LENGTH to
        listOf(
          ConversionUnit("m", "Meter", "m", 1.0),
          ConversionUnit("km", "Kilometer", "km", 1000.0),
          ConversionUnit("cm", "Centimeter", "cm", 0.01),
          ConversionUnit("mm", "Millimeter", "mm", 0.001),
          ConversionUnit("um", "Micrometer", "μm", 1e-6),
          ConversionUnit("mi", "Mile", "mi", 1609.344),
          ConversionUnit("yd", "Yard", "yd", 0.9144),
          ConversionUnit("ft", "Foot", "ft", 0.3048),
          ConversionUnit("in", "Inch", "in", 0.0254),
          ConversionUnit("nm", "Nautical Mile", "nmi", 1852.0),
        ),
      UnitCategory.MASS to
        listOf(
          ConversionUnit("kg", "Kilogram", "kg", 1.0),
          ConversionUnit("g", "Gram", "g", 0.001),
          ConversionUnit("mg", "Milligram", "mg", 1e-6),
          ConversionUnit("lb", "Pound", "lb", 0.45359237),
          ConversionUnit("oz", "Ounce", "oz", 0.028349523125),
          ConversionUnit("t", "Metric Ton", "t", 1000.0),
        ),
      UnitCategory.TEMPERATURE to
        listOf(
          ConversionUnit("c", "Celsius", "°C"),
          ConversionUnit("f", "Fahrenheit", "°F"),
          ConversionUnit("k", "Kelvin", "K"),
        ),
      UnitCategory.ANGLE to
        listOf(
          ConversionUnit("deg", "Degree", "°", 1.0),
          ConversionUnit("rad", "Radian", "rad", 180.0 / Math.PI),
          ConversionUnit("grad", "Gradian", "grad", 0.9),
          ConversionUnit("turn", "Turn", "rev", 360.0),
        ),
      UnitCategory.ENERGY to
        listOf(
          ConversionUnit("j", "Joule", "J", 1.0),
          ConversionUnit("kj", "Kilojoule", "kJ", 1000.0),
          ConversionUnit("cal", "Calorie", "cal", 4.184),
          ConversionUnit("kcal", "Kilocalorie", "kcal", 4184.0),
          ConversionUnit("wh", "Watt-hour", "Wh", 3600.0),
          ConversionUnit("kwh", "Kilowatt-hour", "kWh", 3_600_000.0),
          ConversionUnit("ev", "Electronvolt", "eV", 1.602176634e-19),
        ),
      UnitCategory.SPEED to
        listOf(
          ConversionUnit("ms", "Meters/sec", "m/s", 1.0),
          ConversionUnit("kmh", "Kilometers/hr", "km/h", 1.0 / 3.6),
          ConversionUnit("mph", "Miles/hr", "mph", 0.44704),
          ConversionUnit("kn", "Knot", "kn", 1852.0 / 3600.0),
          ConversionUnit("mach", "Mach (sea lvl)", "Ma", 340.29),
        ),
      UnitCategory.DATA to
        listOf(
          ConversionUnit("b", "Byte", "B", 1.0),
          ConversionUnit("kb", "Kilobyte", "KB", 1024.0),
          ConversionUnit("mb", "Megabyte", "MB", 1048576.0),
          ConversionUnit("gb", "Gigabyte", "GB", 1073741824.0),
          ConversionUnit("tb", "Terabyte", "TB", 1099511627776.0),
        ),
    )

  val physicalConstants: List<PhysicalConstant> =
    listOf(
      PhysicalConstant("c", "Speed of light", 299_792_458.0, "m/s", "299792458"),
      PhysicalConstant("g", "Standard gravity", 9.80665, "m/s²", "9.80665"),
      PhysicalConstant("h", "Planck constant", 6.62607015e-34, "J·s", "6.62607015E-34"),
      PhysicalConstant("G", "Gravitational constant", 6.67430e-11, "m³/(kg·s²)", "6.6743E-11"),
      PhysicalConstant("Nₐ", "Avogadro constant", 6.02214076e23, "mol⁻¹", "6.02214076E23"),
      PhysicalConstant("k", "Boltzmann constant", 1.380649e-23, "J/K", "1.380649E-23"),
      PhysicalConstant("R", "Ideal gas constant", 8.314462618, "J/(mol·K)", "8.314462618"),
      PhysicalConstant("e₀", "Elementary charge", 1.602176634e-19, "C", "1.602176634E-19"),
      PhysicalConstant("φ", "Golden ratio", (1.0 + kotlin.math.sqrt(5.0)) / 2.0, "dimensionless", "φ"),
    )

  fun convert(
    value: Double,
    category: UnitCategory,
    from: ConversionUnit,
    to: ConversionUnit,
  ): Double {
    if (from.id == to.id) return value
    if (category == UnitCategory.TEMPERATURE) {
      val celsius =
        when (from.id) {
          "c" -> value
          "f" -> (value - 32.0) * 5.0 / 9.0
          "k" -> value - 273.15
          else -> value
        }
      return when (to.id) {
        "c" -> celsius
        "f" -> celsius * 9.0 / 5.0 + 32.0
        "k" -> celsius + 273.15
        else -> celsius
      }
    }
    val baseValue = value * from.toBaseFactor
    return baseValue / to.toBaseFactor
  }
}
