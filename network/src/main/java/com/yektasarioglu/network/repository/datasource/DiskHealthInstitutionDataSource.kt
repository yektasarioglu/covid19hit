package com.yektasarioglu.network.repository.datasource

import android.util.Log

import com.yektasarioglu.network.INSTITUTION_LIST
import com.yektasarioglu.network.model.Institution

import io.paperdb.Paper

class DiskHealthInstitutionDataSource : HealthInstitutionOperations {

    private val TAG = "DiskHealthInstitutionDS"

    override suspend fun getHealthInstitutionList() : ArrayList<Institution>? {
        Log.i(TAG, "getHealthInstitutionList()")

        return Paper.book().read<ArrayList<Institution>>(INSTITUTION_LIST)
    }

}