package app.noxlingo.mobile.v2;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.util.ArrayList;
import java.util.Locale;

@CapacitorPlugin(
    name = "NoxSpeech",
    permissions = {@Permission(alias = "microphone", strings = {Manifest.permission.RECORD_AUDIO})}
)
public class NoxSpeechPlugin extends Plugin {
    private SpeechRecognizer recognizer;
    private PluginCall activeCall;
    private boolean listening;

    @PluginMethod
    public void status(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            JSObject response = new JSObject();
            response.put("available", SpeechRecognizer.isRecognitionAvailable(getContext()));
            response.put("permissionGranted", getPermissionState("microphone") == PermissionState.GRANTED);
            boolean local = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && SpeechRecognizer.isOnDeviceRecognitionAvailable(getContext());
            response.put("onDeviceServiceAvailable", local);
            response.put("locale", "es-MX");
            call.resolve(response);
        });
    }

    @PluginMethod
    public void start(PluginCall call) {
        if (getPermissionState("microphone") != PermissionState.GRANTED) {
            requestPermissionForAlias("microphone", call, "microphonePermissionCallback");
            return;
        }
        startRecognition(call);
    }

    @PermissionCallback
    private void microphonePermissionCallback(PluginCall call) {
        if (getPermissionState("microphone") != PermissionState.GRANTED) {
            call.reject("Permissão do microfone negada. Autorize o NoxLingo nas configurações do Android.", "MIC_PERMISSION_DENIED");
            return;
        }
        startRecognition(call);
    }

    private void startRecognition(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            if (listening || activeCall != null) {
                call.reject("O microfone já está escutando.", "MIC_BUSY");
                return;
            }
            if (!SpeechRecognizer.isRecognitionAvailable(getContext())) {
                call.reject("Não foi encontrado um serviço de reconhecimento de voz neste Android.", "RECOGNIZER_UNAVAILABLE");
                return;
            }
            try {
                // Novo recognizer por sessão: evita reutilizar serviço em estado de erro.
                recognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
                recognizer.setRecognitionListener(new RecognitionListener() {
                    @Override public void onReadyForSpeech(Bundle params) {}
                    @Override public void onBeginningOfSpeech() {}
                    @Override public void onRmsChanged(float rmsdB) {}
                    @Override public void onBufferReceived(byte[] buffer) {}
                    @Override public void onEndOfSpeech() {}
                    @Override public void onPartialResults(Bundle partialResults) {}
                    @Override public void onEvent(int eventType, Bundle params) {}

                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> options = results == null ? null :
                            results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (options == null || options.isEmpty() || options.get(0).trim().isEmpty()) {
                            fail("Não consegui reconhecer a frase. Tente novamente.", "NO_MATCH");
                            return;
                        }
                        PluginCall pending = activeCall;
                        activeCall = null;
                        listening = false;
                        releaseRecognizer();
                        if (pending != null) {
                            JSObject value = new JSObject();
                            value.put("text", options.get(0));
                            value.put("language", "es-MX");
                            value.put("alternativeCount", options.size());
                            pending.resolve(value);
                        }
                    }

                    @Override
                    public void onError(int error) {
                        String explanation;
                        String code;
                        switch (error) {
                            case SpeechRecognizer.ERROR_NO_MATCH:
                            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                explanation = "Não ouvi uma frase clara em espanhol. Tente novamente.";
                                code = "NO_MATCH";
                                break;
                            case SpeechRecognizer.ERROR_NETWORK:
                            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                                explanation = "O serviço de reconhecimento precisa de conexão ou de um pacote de voz instalado.";
                                code = "RECOGNIZER_NETWORK";
                                break;
                            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                                explanation = "Permissão do microfone não concedida.";
                                code = "MIC_PERMISSION_DENIED";
                                break;
                            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                                explanation = "O reconhecimento está ocupado. Tente novamente.";
                                code = "MIC_BUSY";
                                break;
                            case SpeechRecognizer.ERROR_CLIENT:
                                explanation = "O Android interrompeu o reconhecimento. Tente novamente.";
                                code = "RECOGNIZER_CLIENT";
                                break;
                            default:
                                explanation = "O reconhecimento de voz falhou (código Android " + error + ").";
                                code = "RECOGNIZER_ERROR";
                                break;
                        }
                        fail(explanation, code);
                    }
                });

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX");
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-MX");
                intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
                // A preferência offline é apenas uma dica ao serviço de reconhecimento.
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, call.getBoolean("preferOffline", false));
                activeCall = call;
                listening = true;
                recognizer.startListening(intent);
            } catch (Exception exception) {
                fail("Não foi possível iniciar o microfone: " + exception.getMessage(), "RECOGNIZER_START_FAILED");
            }
        });
    }

    @PluginMethod
    public void stop(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            if (recognizer != null && listening) {
                try {
                    recognizer.stopListening(); // onResults entrega a transcrição
                } catch (Exception e) {
                    fail("Não foi possível interromper a escuta.", "RECOGNIZER_STOP_FAILED");
                }
            }
            call.resolve();
        });
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            PluginCall pending = activeCall;
            activeCall = null;
            listening = false;
            releaseRecognizer();
            if (pending != null) pending.reject("Gravação cancelada.", "RECOGNIZER_CANCELLED");
            call.resolve();
        });
    }

    private void fail(String message, String code) {
        PluginCall pending = activeCall;
        activeCall = null;
        listening = false;
        releaseRecognizer();
        if (pending != null) pending.reject(message, code);
    }

    private void releaseRecognizer() {
        if (recognizer != null) {
            try {
                recognizer.cancel();
                recognizer.destroy();
            } catch (Exception ignored) {}
            recognizer = null;
        }
    }

    @Override
    protected void handleOnDestroy() {
        PluginCall pending = activeCall;
        activeCall = null;
        listening = false;
        releaseRecognizer();
        if (pending != null) pending.reject("Aplicativo encerrado.", "RECOGNIZER_CANCELLED");
        super.handleOnDestroy();
    }
}
