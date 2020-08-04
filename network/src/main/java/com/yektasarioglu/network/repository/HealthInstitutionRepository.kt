package com.yektasarioglu.network.repository

import android.util.Log

import com.yektasarioglu.network.model.Institution
import com.yektasarioglu.network.repository.datasource.DiskHealthInstitutionDataSource
import com.yektasarioglu.network.repository.datasource.HealthInstitutionOperations
import com.yektasarioglu.network.repository.datasource.RemoteHealthInstitutionDataSource
import com.yektasarioglu.network.utils.getIsConnected
import com.yektasarioglu.network.utils.isInstitutionListCached

class HealthInstitutionRepository(
    private val remoteHealthInstitutionDataSource: RemoteHealthInstitutionDataSource,
    private val diskHealthInstitutionDataSource: DiskHealthInstitutionDataSource
) : HealthInstitutionOperations {

    private val TAG = "HealthInstitutionRepo"

    override suspend fun getHealthInstitutionList() : ArrayList<Institution>? {
        Log.i(TAG, "isInstitutionListCached is $isInstitutionListCached")
        Log.i(TAG, "isConnected is ${getIsConnected()}")

        return if (!isInstitutionListCached && getIsConnected())
            remoteHealthInstitutionDataSource.getHealthInstitutionList()
        else
            diskHealthInstitutionDataSource.getHealthInstitutionList()
    }

    
}