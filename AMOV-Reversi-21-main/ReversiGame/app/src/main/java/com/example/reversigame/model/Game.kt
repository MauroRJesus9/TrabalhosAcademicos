package com.example.reversigame.model

class Game {
    val BOARD_SIZE = 8

    private val CENTER_LEFT_UP = Place(BOARD_SIZE / 2 - 1, BOARD_SIZE / 2 - 1, Stone.BLACK)
    private val CENTER_LEFT_UNDER = Place(BOARD_SIZE / 2 - 1, BOARD_SIZE / 2, Stone.WHITE)
    private val CENTER_RIGHT_UP = Place(BOARD_SIZE / 2, BOARD_SIZE / 2 - 1, Stone.WHITE)
    private val CENTER_RIGHT_UNDER = Place(BOARD_SIZE / 2, BOARD_SIZE / 2, Stone.BLACK)

    val boardStatus = arrayOfNulls<List<Place>>(BOARD_SIZE).mapIndexed { x, list -> arrayOfNulls<Place>(BOARD_SIZE).mapIndexed { y, place -> Place(x, y, Stone.NONE) } }

    fun getInitialPlaces() = listOf(CENTER_LEFT_UP, CENTER_LEFT_UNDER, CENTER_RIGHT_UP, CENTER_RIGHT_UNDER)

    fun canPut(place: Place) = boardStatus[place.x][place.y].stone == Stone.NONE && getCanChangePlaces(place).isNotEmpty()

    fun getAllCanPutPlaces(color: Stone) = boardStatus.flatMap { it }.filter { canPut(Place(it.x, it.y, color)) }

    fun canNext(color: Stone): Boolean = getAllCanPutPlaces(color).isNotEmpty()

    fun countStones(color: Stone) = boardStatus.flatMap { it }.count { it.stone == color }

    fun isGameOver() = !canNext(Stone.BLACK) && !canNext(Stone.WHITE)

    fun getCanChangePlaces(target: Place): List<Place> {
        return searchChangePlacesRight(target)
            .plus(searchChangePlacesLeft(target))
            .plus(searchChangePlacesUp(target))
            .plus(searchChangePlacesUnder(target))
            .plus(searchChangePlacesUpperLeft(target))
            .plus(searchChangePlacesDownRight(target))
            .plus(searchChangePlacesUpperRight(target))
            .plus(searchChangePlacesDownLeft(target))
    }


    private fun searchChangePlacesRight(target: Place): List<Place> {
        if (target.x + 1 > BOARD_SIZE - 1) return emptyList()

        val rightPlaces = boardStatus.drop(target.x + 1)
            .map { it[target.y] }
        return getInsidePlaces(target, rightPlaces)
    }


    private fun searchChangePlacesLeft(target: Place): List<Place> {
        if (target.x == 0) return emptyList()

        val leftPlaces = boardStatus
            .take(target.x)
            .map { it[target.y] }
            .reversed()
        return getInsidePlaces(target, leftPlaces)
    }


    private fun searchChangePlacesUnder(target: Place): List<Place> {
        if (target.y + 1 > BOARD_SIZE - 1) return emptyList()

        val underPlaces = boardStatus[target.x].drop(target.y + 1)
        return getInsidePlaces(target, underPlaces)
    }


    private fun searchChangePlacesUp(target: Place): List<Place> {
        if (target.y == 0) return emptyList()

        val upPlaces = boardStatus[target.x]
            .take(target.y)
            .reversed()
        return getInsidePlaces(target, upPlaces)
    }


    private fun searchChangePlacesUpperLeft(target: Place): List<Place> {
        if (target.x == 0 || target.y == 0) return emptyList()

        val upperLeftPlaces = boardStatus.flatMap { it }
            .filter { it.x < target.x && it.y < target.y }
            .filter { it.x - it.y == target.x - target.y }
            .reversed()
        return getInsidePlaces(target, upperLeftPlaces)
    }


    private fun searchChangePlacesDownRight(target: Place): List<Place> {
        if (target.x + 1 > BOARD_SIZE - 1 || target.y + 1 > BOARD_SIZE - 1) return emptyList()

        val downRightPlaces = boardStatus.flatMap { it }
            .filter { it.x > target.x && it.y > target.y }
            .filter { it.x - it.y == target.x - target.y }
        return getInsidePlaces(target, downRightPlaces)
    }


    private fun searchChangePlacesUpperRight(target: Place): List<Place> {
        if (target.x + 1 > BOARD_SIZE || target.y == 0) return emptyList()

        val upperRightPlaces = boardStatus.flatMap { it }
            .filter { it.x > target.x && it.y < target.y }
            .filter { it.x + it.y == target.x + target.y }
        return getInsidePlaces(target, upperRightPlaces)
    }


    private fun searchChangePlacesDownLeft(target: Place): List<Place> {
        if (target.x == 0 || target.y + 1 > BOARD_SIZE - 1) return emptyList()

        val downLeftPlaces = boardStatus.flatMap { it }
            .filter { it.x < target.x && it.y > target.y }
            .filter { it.x + it.y == target.x + target.y }
            .reversed()
        return getInsidePlaces(target, downLeftPlaces)
    }


    private fun getInsidePlaces(target: Place, places: List<Place>): List<Place> {
        val endPoint = places.indexOfFirst { it.stone == target.stone }

        if (endPoint == -1) return emptyList()

        val insidePlaces = places.take(endPoint)

        if (insidePlaces.all { it.stone == target.stone.other() }) {
            return insidePlaces.map { Place(it.x, it.y, target.stone) }
        }
        return emptyList()
    }
}