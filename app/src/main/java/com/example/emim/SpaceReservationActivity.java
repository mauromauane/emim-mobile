package com.example.emim;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SpaceReservationActivity extends AppCompatActivity{
    private ViewFlipper formSteps;
    private ImageView stepIcon1, stepIcon2, stepIcon3;
    private Button btnAnterior, btnProximo;
    private int currentStep = 0;
    private final int TOTAL_STEPS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.space_reservation_layout);

        formSteps = findViewById(R.id.formSteps);
        stepIcon1 = findViewById(R.id.stepIcon1);
        stepIcon2 = findViewById(R.id.stepIcon2);
        stepIcon3 = findViewById(R.id.stepIcon3);

        btnAnterior = findViewById(R.id.btnAnterior);
        btnProximo = findViewById(R.id.btnProximo);

        updateStepIcons();

        btnAnterior.setOnClickListener(view -> {
            if (currentStep > 0) {
                currentStep--;
                formSteps.setInAnimation(this, android.R.anim.slide_in_left);
                formSteps.setOutAnimation(this, android.R.anim.slide_out_right);
                formSteps.showPrevious();
                updateStepIcons();
            }
        });

        btnProximo.setOnClickListener(view -> {
            if (currentStep < TOTAL_STEPS - 1) {
                currentStep++;
                formSteps.setInAnimation(this, android.R.anim.slide_in_left);
                formSteps.setOutAnimation(this, android.R.anim.slide_out_right);
                formSteps.showNext();
                updateStepIcons();
            } else {
                // Última etapa - submit
                enviarFormulario();
            }
        });
    }

    private void updateStepIcons() {
        // Resetar tudo para cinza
        stepIcon1.setBackgroundResource(R.drawable.circle_gray);
        stepIcon2.setBackgroundResource(R.drawable.circle_gray);
        stepIcon3.setBackgroundResource(R.drawable.circle_gray);

        stepIcon1.setImageResource(R.drawable.ic_baseline_check_24);
        stepIcon2.setImageResource(R.drawable.ic_baseline_check_24);
        stepIcon3.setImageResource(R.drawable.ic_baseline_done_all_24); // Ou ic_check se for concluído

        // Atualizar status com base na etapa atual
        switch (currentStep) {
            case 0:
                stepIcon1.setBackgroundResource(R.drawable.circle_green);
                stepIcon1.setImageResource(R.drawable.ic_baseline_check_24);
                break;
            case 1:
                stepIcon1.setBackgroundResource(R.drawable.circle_green);
                stepIcon2.setBackgroundResource(R.drawable.circle_green);
                break;
            case 2:
                stepIcon1.setBackgroundResource(R.drawable.circle_green);
                stepIcon2.setBackgroundResource(R.drawable.circle_green);
                stepIcon3.setBackgroundResource(R.drawable.circle_green);
                stepIcon3.setImageResource(R.drawable.ic_baseline_check_24);
                break;
        }

        // Atualizar texto do botão "Próximo" para "Finalizar" na última etapa
        btnProximo.setText(currentStep == TOTAL_STEPS - 1 ? "Finalizar" : "Próximo");

        // Esconde o botão "Anterior" na primeira etapa
        btnAnterior.setVisibility(currentStep == 0 ? View.GONE : View.VISIBLE);
    }

    private void enviarFormulario() {
        // Lógica de envio dos dados aqui
        // Pode incluir validação, envio para API etc.
        // Por enquanto, apenas uma simulação:
        System.out.println("Formulário enviado com sucesso!");
        finish(); // fecha a activity
    }
}
