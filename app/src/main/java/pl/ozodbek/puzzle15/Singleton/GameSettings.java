package pl.ozodbek.puzzle15.Singleton;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Switch;

import pl.ozodbek.puzzle15.R;


//O'ZGARUVCHI VA KERAKLI VOSITALAR ELON QILINDI...

@SuppressWarnings("ALL")
public class GameSettings {
    private static GameSettings instance;
    private final int gameBtnSound, dialogBtnSound, statusBtnSound, saveBtnSound, gameWinBtn;
    private boolean soundEnabled, musicEnabled, vibrationEnabled, themeEnabled;
    private final SoundPool soundPool;
    private final Vibrator vibrator;
    private final Context context;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // NEGA CONTEX ISHLATILDI, SABABI BU KALIT SOZ BIZGA DASTURNINING MAXSUS RESURSLARIGA, SERVISLARIGA, CLASSSLARIGA
    // VA BAZI BIR TOP LEVEL OPERATSIYALARGA KIRISHDA YORDAM BERADI, SHUNINGDEK U BIZGA  SHAREDPREFERENSES, PACAGE NAME..
    // VA SHU KABILARGA KIRISHDA YORDAM BERADI. AMMO UNING YOMON TOMONI MEMORY LEAK(XOTIRA YETISHMOVCHILIGI) - NI KELTIRADI.
    // SABABI DASTUR DESTROYED BO'LGACH SINGLETON XOTIRADAGI REFERENCLARNI CONTEX-GA BERADI, U ESA GARBAGAE COLLECTOR BN OCHIRILMAYDI.
    // SHUNING UCHUN XOTIRANI TO'LDIRADI VA MUAMMO CHIQARADI. ALTERNATIVE TARZDA BIZ  getAplicationContex() DAN FOYDALANSAK BOLADI.
    // CHUNKI Contex-DA SINGLETON ACTIVITY REFERNCE SAQLAYDI, getApplicationContex()-DA ESA MAXSUS ACTIVITY SAQLANMAYDI.
    // AGAR BIZDA KATTA MIQDORDA ACTIVITY SINGLETONDA SAQLANDA MEMORY LEAK BOALDI, BOSHQA PAYT BEMALOL ISHLATAMIZ..

    private GameSettings(Context context) {
        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE); // VIBRATSIYA SERVISI
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);               // OVOZ SERVISI

        // ESHTRILADIGAN OVOZLAR ,ULARNI SHU SINGLETON NOMI GLOBAL OZGARUVCHI QIB ELON QILINGAN SHU OVOZLARNING
        // GETTERLARI ORQALI CHQIRILADI..
        gameBtnSound = soundPool.load(context, R.raw.swap_sound, 1);
        dialogBtnSound = soundPool.load(context, R.raw.dialog_button_sounds, 1);
        statusBtnSound = soundPool.load(context, R.raw.status_button_sound, 1);
        saveBtnSound = soundPool.load(context, R.raw.save_button_sound, 1);
        gameWinBtn = soundPool.load(context, R.raw.winner_sound, 1);



        // SHAREDPREFERCES-DAN  "gameSettings" NOMLI JOY OCHILADI, CONTEX NEGA ISHLATILGANI TEPADA AYTILDI
        // MODE_PRIVATE BIZGA BOSHQA JOYDAN USHBU SHAREDPREFERENSES-GA KIRIB BOLMASLIGINI BILDIRADI..
        prefs = context.getSharedPreferences("gameSettings", Context.MODE_PRIVATE);
        editor = prefs.edit(); // BIZ PREFS-GA NARSA YOZAMIZ SHUNGA EDITORDAN RUHSAK OLDIK..


        // NIMA UCHUN AVVAL getBoolean()  ISHLATILYAPTI, SABABI SHAREDPREF OCHILGACH UNING ICHI BO'SH BO'LADI, UNGA BIROR NIMA SAQLASH UCHUN
        // ODATDA PUT BILAN JOYLASHMIZ, AMMAO BIZDA HOLAT BOSHQACHA . BIZ DASTURIMIZNI HAR SAFAR ISHGA TUSHURSA YANGITDAN EMAS, BIRINCHI QOYILGAN MALUMOTNI
        // USER OCHIRGUNICHA SAQLAMOQCHIMIZ, UNI SAQLASH UCHUN ESA BIZGA VARIABLE KERAK BOLADI. BU HOLATDA VARIABLE SOUN VA VIBARTION ENABLED-LAR.
        // IKKALASI DASTURGA ISHGA TUSHGACH BIRINCHI BERILGAN BUYRUQNI OZIDA SAQLAYDI, VA QAYERDA KERAK BOLSA ,VARIABLE ORQALI CHAQIRILADI.
        // NEGA TRUE ? SABABI DATURGA BIRINCHI BOR ISHGA TUSHSA OVOZLI HOLATDA ISHLAYDI, FALSE QILSAK AKSINCHA...

        soundEnabled = prefs.getBoolean("soundEnabled", true);
        vibrationEnabled = prefs.getBoolean("vibrationEnabled", true);

    }


    // BUYERDA NIMA YUZ BERYAPTI ?  GETINSTANCE() METODI BIZGA BITTA UMUMIY OYEKT YARATADI, VA SHU PROYEKT
    // ICHIDA BIR XIL OBYEKTLAR OLIB , KO'P JOYLARDA ISHLATILGAN XOLATLARNI KAMAYTIRISH ORQALI HEAP HOTIRASIGA
    // YENGILLIK , QOLAVERSA KODNI HAM YOZISHGA OSONLASHTIRIB BERADI, KOPROQ DATABASE VA NETWORK CONNECTION-LARDA ISHLATIALDI.
    // IF ICHIDA AGAR TEPADA STATIC GLOBAL INSTANCE NOMILI OZGARUVCHIDA QIYMAT BOLMASA, YANI ICHI BOLSA, YANGISINI CONTEX-DAN OLIB YARATADI.
    // IF BOSH BOLMASA , SHUNI QAYTARADI...

    public static GameSettings getInstance(Context context) {
        if (instance == null) {
            instance = new GameSettings(context);
        }
        return instance;
    }

    // BUYERDA BOOLEAN METHOD BOR , SHAREDPREF-GA AVVALDAN SAQLANGAN BUYRUQNI OLIB QAYTARISH UCHUN..

    public boolean isSoundEnabled() {
        return soundEnabled;
    }


    // BU METHOD SWITCH-DA ISHLATILGAN, BOOLEAN OZGARUVCHI QABUL QILADI, KELAGN OZGARUVCHI TEPADAGI GLOBALGA TENG ,
    // BU DEGANI SHAREDPREF-DAN HAM OZGARADI DEGANI, SABABI UNI SHAREDPREFGA VARIABLE QIB SAQLAB QOYGANMIZ
    // SHAREDPREFGA-GA YZOISHGA RUHSAT OLIB, EDITGA JOYLAGAN EDIK. ENDI U ORQALI BIZGA SWITCHDAM KELGAN OZGARUVCHINI
    // QANDAYDIR("soundEnabled") KALIT BILAN BERKIRIB QOYAMIZ, YANI SHAREDPREFDAGI ESKI QIYMAT SWITCHDAN KELAGN QIYMATGA TENG BOLIB QOALDI
    // APPLY() ORQALI FAQAT SHU METHOD UCHUN EDITNI YAKUNLAYDI, AGAR COMMIT QILSAK BOSHQA UNGA YOZIB BOLMASIDA..
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
        editor.putBoolean("soundEnabled", soundEnabled);
        editor.apply();
    }


    //BUNDA ESA VIBRATSIYA UCHUN SOUND KABI YOZILADI..
    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }


    //BUNDA HAM...
    public void setVibrationEnabled(boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
        editor.putBoolean("vibrationEnabled", vibrationEnabled);
        editor.apply();
    }


    // QAYSI ACTIVITYDA SOUND YARATMOQCHI BOLSAK, AVVAL SINGLETONNI NOMI VA SHU METHOD NOMI BILAN CHAQIRILADI...
    // -->> gameSettings.playSound(gameSettings.getStatusBtnSound());
    public void playSound(int soundId) {
        if (soundEnabled) {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }


    // QAYSI ACTIVITYDA VIBRATION YARATMOQCHI BOLSAK, AVVAL SINGLETONNI NOMI VA SHU METHOD NOMI BILAN CHAQIRILADI...
    // -->> gameSettings.vibration();
    public void vibrate() {
        if (vibrationEnabled) {
            final VibrationEffect vibrationEffect4;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                vibrationEffect4 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
                vibrator.cancel();
                vibrator.vibrate(vibrationEffect4);
            }
        }
    }


    // BULAR ESA BIZGA SOUND VA VIBRATION-NI OZINI OLIB KELITIRB BERADI
    //// -->> gameSettings.playSound(gameSettings.getStatusBtnSound());

    public int getGameBtnSound() {
        return gameBtnSound;
    }

    public int getDialogBtnSound() {
        return dialogBtnSound;
    }

    public int getStatusBtnSound() {
        return statusBtnSound;
    }

    public int getSaveBtnSound() {
        return saveBtnSound;
    }

    public int getGameWinBtn() {
        return gameWinBtn;
    }


}