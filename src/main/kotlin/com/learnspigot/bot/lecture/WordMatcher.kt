package com.learnspigot.bot.lecture

class WordMatcher {
    fun getTopLectures(query: String, source: List<Lecture>, amount: Int = 1): List<Lecture> {
        val top = getTopMatches(query, source.map { it.title }, amount)
        val map = source.associateBy { it.title }
        return top.map { map[it]!! }
    }

    fun getTopMatches(word: String, source: List<String>, amount: Int = 1) = getSorted(word, source).take(amount)

    fun getBestMatch(word: String, source: List<String>): String {
        val matcher = WordMatcher()
        return source.maxBy { matcher.getMatchScore(word, it) }
    }

    private fun getSorted(word: String, source: List<String>): List<String> {
        val matcher = WordMatcher()
        return source.sortedByDescending { matcher.getMatchScore(word, it) }
    }

    /**
     * Generate a score between 0 and 4.5 determining how similar "query" is to "check".
     * @param query the word being scored
     * @param check the word being scored against
     */
    fun getMatchScore(query: String, check: String): Double {
        var test = query.lowercase()
        var check = check.lowercase()
        val wordMatchPercentage = test.split(" ").let { words -> words.count { check.contains(it) } / words.size.toDouble()  }
        val FULL_WORD_BIAS = 3
        val LENGTH_BIAS = 0.25

        return average(
            FULL_WORD_BIAS*wordMatchPercentage, // Increase score significantly if the title contains fully any words from the query.
            getAdjustedCharacterSimilarity(test, check), // Checks spelling mistakes, using an unordered character similarity.
            LENGTH_BIAS * getAmbiguousPortion(test.length, check.length) // Slightly pioritise words with similar length.
        )
    }

    /**
     * Compares two strings by comparing the words within them.
     * This will ensure that matching the query 'Scoreboard' to 'Custom Scoreboards' does not yield undesired results
     */
    private fun getAdjustedCharacterSimilarity(query: String, title: String): Double {
        var total = 0.0
        val titleWords = title.split(" ")
        val queryWords = query.split(" ")
        // For each word in the query, add the similarity of the most similar word in the title.
        for (word in queryWords) total += titleWords.maxOf { getRawCharacterSimilarity(word, it) }
        return total/queryWords.size
    }

    /**
     * Uses a raw character similarity to compare two words
     */
    private fun getRawCharacterSimilarity(word1: String, word2: String): Double {
        val uniqueChars: HashSet<Char> = HashSet()
        val chars1 = word1.toCharArray()
        val chars2 = word2.toCharArray()
        uniqueChars.addAll(chars1.distinct())
        uniqueChars.addAll(chars2.distinct())

        var total = 0.0
        for (character in uniqueChars){ // Add the proportion of each distinct character in t
            total += getAmbiguousPortion(chars1.count { it == character }, chars2.count { it == character })
        }
        return total/uniqueChars.size
    }

    private fun average(vararg vals: Number): Double {
        var total = 0.0
        vals.forEach { total += it.toDouble() }
        return total/vals.size
    }

    private fun getAmbiguousPortion(amount1: Number, amount2: Number) = minOf(amount1.toDouble(), amount2.toDouble()) / maxOf(amount1.toDouble(), amount2.toDouble())
}