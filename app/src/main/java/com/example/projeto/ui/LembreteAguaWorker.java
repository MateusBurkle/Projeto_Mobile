package com.example.projeto.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.projeto.R;
import com.example.projeto.storage.AppDatabase;
import com.example.projeto.storage.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class LembreteAguaWorker extends Worker {

    // Frases divertidas para incentivar a Débora e o Cássio
    private final String[] frases = {
            "A planta precisa de água, você também! 🌵",
            "Vai secar aí? Bebe água logo! 💧",
            "Seu rim mandou um 'oi' e pediu água. 🥤",
            "Não me obrigue a ir aí te dar água... 😠",
            "princesa bebe água, você não é cacto! 🌸",
            "a meta não vai se bater sozinha. Glup glup! 💦",
            "Tá esperando o quê? A desidratação? 💀",
            "Beba água para ficar com a pele de milhões! ✨"
    };

    public LembreteAguaWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SessionManager session = new SessionManager(context);
        AppDatabase db = AppDatabase.getInstance(context);

        // Se não estiver logado, não faz nada
        if (!session.isLoggedIn()) {
            return Result.success();
        }

        try {
            // 1. Calcular a Meta (Peso * 35 ou padrão 2000)
            // --- CORREÇÃO AQUI: mudei de .prefs para .pref ---
            int peso = session.pref.getInt("weight_kg", 0);
            int metaDoDia = (peso > 0) ? peso * 35 : 2000;

            // 2. Calcular quanto já bebeu hoje usando o método do DAO
            String dataHoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // Verifica se tem registro hoje
            int totalIngerido = db.historicoAguaDao().getTotalAguaDoDia(dataHoje);

            // 3. Lógica da Notificação:
            // Se bebeu menos da meta, manda o lembrete!
            if (totalIngerido < metaDoDia) {
                mandarNotificacao(context);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }

        return Result.success();
    }

    private void mandarNotificacao(Context context) {
        String canalId = "canal_lembrete_agua";

        // Criar o canal de notificação (Obrigatório para Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    canalId,
                    "Lembrete de Água",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para te lembrar de beber água");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Escolher frase aleatória
        String frase = frases[new Random().nextInt(frases.length)];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canalId)
                .setSmallIcon(R.drawable.icone_garrafa) // Usa seu ícone de garrafa
                .setContentTitle("Hora da Água! 💧")
                .setContentText(frase)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Verificar permissão antes de enviar (Android 13+)
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(100, builder.build());
        }
    }
}