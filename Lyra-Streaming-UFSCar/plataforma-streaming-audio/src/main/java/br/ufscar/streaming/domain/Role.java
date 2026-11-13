package br.ufscar.streaming.domain;

/**
 * Papéis de controle de acesso baseado em função (RBAC - Seção 2 e RNF007).
 */
public enum Role {
    OUVINTE,
    CRIADOR,
    MODERADOR,
    ADMINISTRADOR
}
