package com.yektasarioglu.covid19hit.utils

import org.hamcrest.CoreMatchers.*

import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test

class StringSimilarityTest {

    @Test
    fun `Similarity should return greater than 78%`() {
        val testSentence = "Linus Torvalds is the king"
        val testSentence2 = "Linus Torvalds is the master"
        val similarityRatio= StringSimilarity.similarity(testSentence, testSentence2)

        assertTrue("Similarity ratio should be greater than 78%", similarityRatio.toFloat() > 0.78)
    }

    @Test
    fun `Similarity should return 0%`() {
        val testSentence = "Linus Torvalds is the king"
        val testSentence2 = ""
        val similarityRatio= StringSimilarity.similarity(testSentence, testSentence2)

        assertTrue("Similarity ratio should be 0%", similarityRatio.toFloat() == 0f)
    }

    @Test
    fun `Similarity should return 100%`() {
        val testSentence = "Test Test Test test"
        val testSentence2 = "Test Test Test test"
        val similarityRatio= StringSimilarity.similarity(testSentence, testSentence2)

        assertThat(similarityRatio.toFloat(), `is`(1.0f))
    }


}