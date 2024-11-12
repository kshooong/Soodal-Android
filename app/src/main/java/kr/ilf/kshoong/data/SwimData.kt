package kr.ilf.kshoong.data

data class SwimData(
    val date: String,
    val freestyle: Int?,
    val backStroke: Int?,
    val breastStroke: Int?,
    val butterfly: Int?,
    val kickBoard: Int?,
    val swimMeters: List<Pair<String, Int?>>
) {
    constructor(
        date: String,
        freestyle: Int?,
        backStroke: Int?,
        breastStroke: Int?,
        butterfly: Int?,
        kickBoard: Int?,
    ) : this(
        date,
        freestyle,
        backStroke,
        breastStroke,
        butterfly,
        kickBoard,
        listOf(
            Pair("freestyle", freestyle),
            Pair("backStroke", backStroke),
            Pair("breastStroke", breastStroke),
            Pair("butterfly", butterfly),
            Pair("kickBoard", kickBoard)
        )
    )
}
