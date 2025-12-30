package com.lxkj.aiim.lj.bean

import java.io.Serializable

class AiCheckResultBean : Serializable {
    var multiModing: MultiModing? = null
    var document: Document? = null
}

data class Document(
    var projectDocument: ProjectDocument? = null,
    var contractDocument: ContractDocument? = null
): Serializable

data class ContractDocument(
    var aiGenerated: Boolean? = null,
    var hiddenTrap: HiddenTrap? = null,
    var jurisdictionCourt: JurisdictionCourt? = null,
    var keyClause: KeyClause? = null,
    var responsibility: Responsibility? = null,
    var riskLevel: String? = null,
    var riskValue: String? = null
): Serializable

data class HiddenTrap(
    var clauseType: String? = null,
    var description: String? = null,
    var isNormal : Boolean =false
): Serializable

data class JurisdictionCourt(
    var description: String? = null,
    var locationTag: String? = null,
    var isNormal : Boolean =false
): Serializable

data class KeyClause(
    var description: String? = null,
    var riskTag: String? = null,
    var isNormal : Boolean =false
): Serializable

data class Responsibility(
    var allocationStatus: String? = null,
    var description: String? = null,
    var isNormal : Boolean =false
): Serializable


data class ProjectDocument(
    var aiGenerated: Boolean=false,
    var businessLogic: BusinessLogic? = null,
    var capitalFlow: CapitalFlow? = null,
    var complianceStatus: ComplianceStatus? = null,
    var riskLevel: String? = null,
    var riskValue: String? = null,
    var teamBackground: TeamBackground? = null
): Serializable

data class BusinessLogic(
    var description: String? = null,
    var riskFeature: String? = null,
    var isNormal : Boolean =false
): Serializable

data class CapitalFlow(
    var description: String? = null,
    var flowStatus: String? = null,
    var isNormal : Boolean =false
): Serializable

data class ComplianceStatus(
    var description: String? = null,
    var filingStatus: String? = null,
    var isNormal : Boolean =false
): Serializable

data class TeamBackground(
    var description: String? = null,
    var status: String? = null,
    var isNormal : Boolean =false
): Serializable


data class MultiModing(
    var aiGenerated: Boolean = false,
    var analysisReport: AnalysisReport? = null,
    var environmentConsistency: EnvironmentConsistency? = null,
    var metadata: Metadata? = null,
    var riskLevel: String? = null,
    var riskValue: String? = null,
    var voiceprintAnalysis: VoiceprintAnalysis? = null
): Serializable

data class AnalysisReport(
    var description: String? = null,
    var verdict: String? = null,
    var isNormal : Boolean =false
): Serializable

data class EnvironmentConsistency(
    var description: String? = null,
    var verdict: String? = null,
    var isNormal : Boolean =false
): Serializable

data class Metadata(
    var description: String? = null,
    var verdict: String? = null,
    var isNormal : Boolean =false
): Serializable

data class VoiceprintAnalysis(
    var description: String? = null,
    var verdict: String? = null,
    var isNormal : Boolean =false
): Serializable

