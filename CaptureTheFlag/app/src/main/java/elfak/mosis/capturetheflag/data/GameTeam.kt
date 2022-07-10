package elfak.mosis.capturetheflag.data

data class GameTeam(
    var teamName: String = "",
    var teamMembers: ArrayList<String> = arrayListOf(),
    var memberCount: Int = 0) {
}