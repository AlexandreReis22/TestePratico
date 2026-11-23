package app.noxlingo.mobile.v2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@CapacitorPlugin(name = "NoxTts")
public class NoxTtsPlugin extends Plugin {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Runnable> pending = new ArrayList<>();
    private TextToSpeech engine;
    private boolean initialized = false;
    private String selectedLocale = "";
    private String selectedVoice = "";

    private void whenReady(Runnable action) {
        mainHandler.post(() -> {
            if (initialized) {
                action.run();
                return;
            }
            pending.add(action);
            if (engine != null) return;
            engine = new TextToSpeech(getContext().getApplicationContext(), status ->
                mainHandler.post(() -> {
                    initialized = status == TextToSpeech.SUCCESS;
                    if (initialized) selectOfflineSpanishVoice();
                    List<Runnable> callbacks = new ArrayList<>(pending);
                    pending.clear();
                    for (Runnable callback : callbacks) callback.run();
                })
            );
        });
    }

    private void selectOfflineSpanishVoice() {
        Set<Voice> available = engine.getVoices();
        if (available == null) return;
        Voice best = available.stream()
            .filter(voice -> "es".equals(voice.getLocale().getLanguage()))
            .filter(voice -> !voice.isNetworkConnectionRequired())
            .max(Comparator.comparingInt(NoxTtsPlugin::rankVoice))
            .orElse(null);
        if (best == null) return;
        int result = engine.setVoice(best);
        if (result == TextToSpeech.SUCCESS) {
            selectedLocale = best.getLocale().toLanguageTag();
            selectedVoice = best.getName();
        }
    }

    private static int rankVoice(Voice voice) {
        String region = voice.getLocale().getCountry().toUpperCase(Locale.ROOT);
        switch (region) {
            case "MX": return 110;
            case "CO": return 105;
            case "AR": return 100;
            case "CL": return 95;
            case "PE": return 90;
            case "US": return 85;
            case "VE": return 80;
            case "UY": return 75;
            case "ES": return 40;
            default: return 50;
        }
    }

    @PluginMethod
    public void speak(PluginCall call) {
        final String message = call.getString("text", "").trim();
        final Double userRate = call.getDouble("rate", 0.85);
        if (message.isEmpty()) {
            call.reject("Texto vazio.", "EMPTY_TEXT");
            return;
        }
        whenReady(() -> {
            if (!initialized) {
                call.reject("O serviço de voz do Android não está disponível.", "TTS_NOT_READY");
                return;
            }
            if (selectedVoice.isEmpty()) selectOfflineSpanishVoice();
            if (selectedVoice.isEmpty()) {
                call.reject("Falta uma voz offline em espanhol. Instale a voz nas configurações de texto para fala do Android.", "SPANISH_VOICE_MISSING");
                return;
            }
            float rate = (float)Math.max(0.60, Math.min(1.25, userRate == null ? 0.85 : userRate));
            engine.setSpeechRate(rate);
            int result = engine.speak(message, TextToSpeech.QUEUE_FLUSH, new Bundle(), UUID.randomUUID().toString());
            if (result == TextToSpeech.ERROR) {
                call.reject("O motor de voz não conseguiu reproduzir o áudio.", "SPEAK_ERROR");
                return;
            }
            JSObject reply = new JSObject();
            reply.put("locale", selectedLocale);
            reply.put("voice", selectedVoice);
            call.resolve(reply);
        });
    }

    @PluginMethod
    public void status(PluginCall call) {
        whenReady(() -> {
            if (initialized && selectedVoice.isEmpty()) selectOfflineSpanishVoice();
            JSObject reply = new JSObject();
            reply.put("available", initialized && !selectedVoice.isEmpty());
            reply.put("locale", selectedLocale);
            reply.put("voice", selectedVoice);
            reply.put("offline", true);
            call.resolve(reply);
        });
    }

    @PluginMethod
    public void stop(PluginCall call) {
        mainHandler.post(() -> {
            if (engine != null && initialized) engine.stop();
            call.resolve();
        });
    }

    @Override
    protected void handleOnDestroy() {
        if (engine != null) {
            engine.stop();
            engine.shutdown();
            engine = null;
            initialized = false;
        }
        super.handleOnDestroy();
    }
}
