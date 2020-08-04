package com.yektasarioglu.network.repository.datasource

import android.util.Log

import com.yektasarioglu.network.INSTITUTION_LIST
import com.yektasarioglu.network.model.Institution

import io.paperdb.Paper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.jsoup.Jsoup

private const val LABS_URL = "https://covid19bilgi.saglik.gov.tr/tr/covid-19-yetkilendirilmis-tani-laboratuvarlari-listesi"
private const val FIRST_INDEX = 1

class RemoteHealthInstitutionDataSource : HealthInstitutionOperations {

    private val TAG = "RemoteHIDS"

    override suspend fun getHealthInstitutionList() = scrapeHealthInstitutionList()

    private suspend fun scrapeHealthInstitutionList(): ArrayList<Institution> = withContext(Dispatchers.IO) {
        Log.i(TAG, "scrapeHealthInstitutionList()")

        val document = Jsoup.connect(LABS_URL)
            .ignoreContentType(true)
            .ignoreHttpErrors(true)
            .get()
        val element = document.select("div.articleBody").first()
        Log.i(TAG, "element.allElements are ${element.allElements}")

        // This line would drop until the first index of element and last in other words we crop
        // the initial table headers which are `NO`, `IL`, `KURUM` headers and last line that has useless information.
        val data = element.select("tr").drop(FIRST_INDEX)

        val institutionList = arrayListOf<Institution>()

        data.forEach {
            institutionList.add(Institution(city = it.child(2).text(), name = it.child(3).text()))
        }

        Log.i(TAG, "institutionList size is ${institutionList.size}")
        institutionList.forEach {
            Log.i(TAG, "City is ${it.city} and Name is ${it.name}")
        }

        Paper.book().write(INSTITUTION_LIST, institutionList)

        institutionList
    }


}