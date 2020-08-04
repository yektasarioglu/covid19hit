package com.yektasarioglu.network.repository.datasource

import com.yektasarioglu.network.model.Institution

interface HealthInstitutionOperations {
    suspend fun getHealthInstitutionList() : ArrayList<Institution>?
}