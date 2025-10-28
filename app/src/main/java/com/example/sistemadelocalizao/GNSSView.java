package com.example.sistemadelocalizao;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GnssStatus;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class GNSSView extends View {
    private GnssStatus gnssStatus = null; // satélites do sistema GNSS
    private int r; // raio da esfera celeste
    private int height, width; // altura e largura do componente
    private final Paint paint = new Paint(); // pincel para o desenho
    private boolean mostrarGps = true;
    private boolean mostrarGlonass = true;
    private boolean mostrarGalileo = true;
    private boolean mostrarBeidou = true;
    private boolean mostrarNaoUsados = true;
    // GPS, Galileo, GLONASS, Beidou

    public GNSSView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        carregarPreferencias();

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoConfiguracao();
            }
        });
        // o tamanho do componente não pode ser inferior no construtor
        // delegar esta funcionalidade para o método onSizeChanged
    }

    public void dialogoConfiguracao() {
        final String[] constelacoes = {"GPS", "GLONASS", "Galileo", "Beidou", "Satélites Não Usados"};
        final boolean[] constelacoesMarcadas = {mostrarGps, mostrarGlonass, mostrarGalileo, mostrarBeidou, mostrarNaoUsados};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Configurar Visualização");
        builder.setMultiChoiceItems(constelacoes, constelacoesMarcadas, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // atualiza o estado da opção no array temporário quando o usuário clica
                constelacoesMarcadas[which] = isChecked;
            }
        });

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mostrarGps = constelacoesMarcadas[0];
                mostrarGlonass = constelacoesMarcadas[1];
                mostrarGalileo = constelacoesMarcadas[2];
                mostrarBeidou = constelacoesMarcadas[3];
                mostrarNaoUsados = constelacoesMarcadas[4];

                salvarPreferencias();
                invalidate(); // força o redesenho da tela com os filtros aplicados
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void salvarPreferencias() {
        SharedPreferences sharePrefs = getContext().getSharedPreferences("GNSSViewPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePrefs.edit();
        editor.putBoolean("mostrar_gps", mostrarGps);
        editor.putBoolean("mostrar_glonass", mostrarGlonass);
        editor.putBoolean("mostrar_galileo", mostrarGalileo);
        editor.putBoolean("mostrar_beidou", mostrarBeidou);
        editor.putBoolean("mostrar_nao_usados", mostrarNaoUsados);
        editor.apply();
    }

    private void carregarPreferencias() {
        SharedPreferences sharePrefs = getContext().getSharedPreferences("GNSSViewPrefs", Context.MODE_PRIVATE);
        // Carrega os valores salvos, se não tiver nada salvo, o padrão é true
        mostrarGps = sharePrefs.getBoolean("mostrar_gps", true);
        mostrarGlonass = sharePrefs.getBoolean("mostrar_glonass", true);
        mostrarGalileo = sharePrefs.getBoolean("mostrar_galileo", true);
        mostrarBeidou = sharePrefs.getBoolean("mostrar_beidou", true);
        mostrarNaoUsados = sharePrefs.getBoolean("mostrar_nao_usados", true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        if (width < height) {
            r = (int) (width / 2 * 0.9);
        } else {
            r = (int) (height / 2 * 0.9);
        }
    }

    public void newStatus(GnssStatus gnssStatus) {
        this.gnssStatus = gnssStatus;
        invalidate(); // força o redesenho do componente
    }

    // métodos para conversão do sistema coordenadas
    private int computeXc(double x) {
        return (int) (x + width / 2);
    }

    private int computeYc(double y) {
        return (int) (-y + height / 2);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // configura o pincel para desenhar a projeção da esfera celeste
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLUE);

        // desenha a projeção de esfera celeste
        // desenhando círculos concêntricos
        canvas.drawCircle(computeXc(0), computeYc(0), r, paint);
        canvas.drawCircle(computeXc(0), computeYc(0), (int) (r * 0.66), paint);
        canvas.drawCircle(computeXc(0), computeYc(0), (int) (r * 0.33), paint);

        // desenhando os eixos
        canvas.drawLine(computeXc(0), computeYc(-r), computeXc(0), computeYc(r), paint);
        canvas.drawLine(computeXc(-r), computeYc(0), computeXc(r), computeYc(0), paint);

        // desenhando os satélites (caso exista um GnssStatus disponível)
        if (gnssStatus != null) {
            int satelitesVisiveis = 0;
            int satelitesEmUso = 0;

            // loop para contar os satélites que passam no filtro
            for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
                if (shouldDrawSatellite(i)) {
                    satelitesVisiveis++;
                    if (gnssStatus.usedInFix(i)) {
                        satelitesEmUso++;
                    }
                }
            }

            // desenha o texto com a contagem correta
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(40);
            canvas.drawText("Visíveis: " + satelitesVisiveis, 50, 100, paint);
            canvas.drawText("Em uso: " + satelitesEmUso, 50, 150, paint);

            // loop para desenhar os satélites que passam no filtro
            for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
                if (shouldDrawSatellite(i)) {
                    int constellation = gnssStatus.getConstellationType(i);
                    boolean used = gnssStatus.usedInFix(i);

                    if (constellation == GnssStatus.CONSTELLATION_GPS) paint.setColor(Color.GREEN);
                    else if (constellation == GnssStatus.CONSTELLATION_GLONASS) paint.setColor(Color.RED);
                    else if (constellation == GnssStatus.CONSTELLATION_GALILEO) paint.setColor(Color.YELLOW);
                    else if (constellation == GnssStatus.CONSTELLATION_BEIDOU) paint.setColor(Color.MAGENTA);
                    else paint.setColor(Color.GRAY);

                    float az = gnssStatus.getAzimuthDegrees(i);
                    float el = gnssStatus.getElevationDegrees(i);
                    float x = (float) (r * Math.cos(Math.toRadians(el)) * Math.sin(Math.toRadians(az)));
                    float y = (float) (r * Math.cos(Math.toRadians(el)) * Math.cos(Math.toRadians(az)));

                    if (used) {
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(computeXc(x), computeYc(y), 15, paint);
                    } else {
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(3);
                        canvas.drawCircle(computeXc(x), computeYc(y), 15, paint);
                    }

                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(30);
                    String satID = gnssStatus.getSvid(i) + "";
                    canvas.drawText(satID, computeXc(x) + 15, computeYc(y) + 15, paint);
                }
            }
        }
    }
    private boolean shouldDrawSatellite(int i) {
        int constellation = gnssStatus.getConstellationType(i);
        boolean used = gnssStatus.usedInFix(i);
        // se o usuário quer esconder os não usados e este satélite não está em uso, então não desenha
        if (!mostrarNaoUsados && !used) {
            return false;
        }
        // verifica a constelação, se a do satélite corresponder a uma que o usuário quer ver, desenha
        switch (constellation) {
            case GnssStatus.CONSTELLATION_GPS:
                return mostrarGps;
            case GnssStatus.CONSTELLATION_GLONASS:
                return mostrarGlonass;
            case GnssStatus.CONSTELLATION_GALILEO:
                return mostrarGalileo;
            case GnssStatus.CONSTELLATION_BEIDOU:
                return mostrarBeidou;
            default:
                // sempre mostrar constelações desconhecidas
                return true;
        }
    }

}