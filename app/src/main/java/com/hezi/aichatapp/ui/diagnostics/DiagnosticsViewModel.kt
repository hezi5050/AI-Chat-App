package com.hezi.aichatapp.ui.diagnostics

import androidx.lifecycle.ViewModel
import com.hezi.aichatapp.data.DiagnosticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val diagnosticsRepository: DiagnosticsRepository
) : ViewModel() {

    fun getDiagnosticsInfo() = diagnosticsRepository.getDiagnosticsInfo()
    
    fun getLogsList() = diagnosticsRepository.getLogsList()
}

