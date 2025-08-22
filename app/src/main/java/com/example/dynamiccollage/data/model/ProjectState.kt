package com.example.dynamiccollage.data.model

data class ProjectState(
    val coverConfig: CoverPageConfig,
    val pageGroups: List<PageGroup>,
    val sunatData: SelectedSunatData?,
    val themeName: String,
    val imageEffectSettings: Map<String, ImageEffectSettings>
)
