package br.ufscar.streaming.domain;

/**
 * Estados do fluxo de instrução e julgamento de denúncias (RF024, RF025 e UC04).
 */
public enum ReportStatus {
    PENDENTE,
    PROCEDENTE_SUSPENSO,
    INDEFERIDA_ARQUIVADA,
    ENCERRADA_PERDA_OBJETO
}
