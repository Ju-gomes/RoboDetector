package br.edu.ifrs.tcc_novo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessaoManager {
    private static final String PREF_NAME = "RoboTrackerSessao";
    private static final String KEY_USER_ID_LOGADO = "user_id_logado";
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessaoManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void salvarSessao(int userId) {
        editor.putInt(KEY_USER_ID_LOGADO, userId);
        editor.commit();
    }

    public boolean estaLogado() {
        return getUserIdLogado() != -1;
    }

    public int getUserIdLogado() {
        return pref.getInt(KEY_USER_ID_LOGADO, -1);
    }

    public void limparSessao() {
        editor.remove(KEY_USER_ID_LOGADO);
        editor.commit();
    }
}