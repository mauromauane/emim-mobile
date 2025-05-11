package com.example.emim;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SpaceReservationActivity extends AppCompatActivity{

        private EditText inputNome, inputFrequencia, inputEndereco, inputLatitude, inputLongitude,
                inputDescricao, inputDescricaoDocumento, inputFormatoDocumento;

        private Spinner spinnerProvincia, spinnerCidade, spinnerBairro, spinnerTipoDocumento;

        private Button btnSave, btnCancel;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.space_reservation_layout);

            // Bind views
            inputNome = findViewById(R.id.inputNome);
            inputFrequencia = findViewById(R.id.inputFrequencia);
            inputEndereco = findViewById(R.id.inputEndereco);
            inputLatitude = findViewById(R.id.inputLatitude);
            inputLongitude = findViewById(R.id.inputLongitude);
            inputDescricao = findViewById(R.id.inputDescricao);
            inputDescricaoDocumento = findViewById(R.id.inputDescricaoDocumento);
            inputFormatoDocumento = findViewById(R.id.inputFormatoDocumento);

            spinnerProvincia = findViewById(R.id.spinnerProvincia);
            spinnerCidade = findViewById(R.id.spinnerCidade);
            spinnerBairro = findViewById(R.id.spinnerBairro);
            spinnerTipoDocumento = findViewById(R.id.spinnerTipoDocumento);

            btnSave = findViewById(R.id.btnSave);
            btnCancel = findViewById(R.id.btnCancel);

            // Setup Spinners with example data
            setupSpinners();

            // Button handlers
            btnSave.setOnClickListener(v -> handleSubmit());
            btnCancel.setOnClickListener(v -> finish());
        }

        private void setupSpinners() {
            String[] provincias = {"Maputo", "Gaza", "Inhambane"};
            String[] cidades = {"Cidade 1", "Cidade 2"};
            String[] bairros = {"Bairro 1", "Bairro 2"};
            String[] tiposDocumento = {"Licença", "Contrato", "Outro"};

            setSpinnerData(spinnerProvincia, provincias);
            setSpinnerData(spinnerCidade, cidades);
            setSpinnerData(spinnerBairro, bairros);
            setSpinnerData(spinnerTipoDocumento, tiposDocumento);
        }

        private void setSpinnerData(Spinner spinner, String[] data) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, data);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        private void handleSubmit() {
            String nome = inputNome.getText().toString().trim();
            String frequencia = inputFrequencia.getText().toString().trim();
            String endereco = inputEndereco.getText().toString().trim();
            String latitude = inputLatitude.getText().toString().trim();
            String longitude = inputLongitude.getText().toString().trim();

            if (nome.isEmpty() || frequencia.isEmpty() || endereco.isEmpty()
                    || latitude.isEmpty() || longitude.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Submit data to backend or save locally

            Toast.makeText(this, "Reserva salva com sucesso!", Toast.LENGTH_LONG).show();
            finish(); // Close activity
        }
}
