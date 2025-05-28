package br.com.superid.auth

import javax.crypto.SecretKey

object SessionManager {
    // Guarda a chave derivada da senha-mestra do usuário logado
    var secretKey: SecretKey? = null

    // (Opcional) Guardar o UID do usuário logado
    var currentUid: String? = null

    // Limpa todos os dados da sessão (chame no logout!)
    fun clear() {
        secretKey = null
        currentUid = null
    }
}