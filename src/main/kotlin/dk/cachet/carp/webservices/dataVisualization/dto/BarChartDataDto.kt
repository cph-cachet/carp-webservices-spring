package dk.cachet.carp.webservices.dataVisualization.dto

class BarChartDataDto {
    val labels = mutableMapOf<String, String>()
    val timeSeries: MutableList<TimeSeriesEntryDto> = mutableListOf()
}