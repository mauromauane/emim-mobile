package com.example.emim;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class SpaceReservationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ViewFlipper formSteps;
    private int stepIndex = 0;

    private Button btnAnterior, btnProximo, btnExportPdf;

    // Step 1 fields
    private TextInputEditText inputEndereco, inputLatitude, inputLongitude;
    private Spinner spinnerFrequency;

    private TextInputLayout inputNome;

    // Step 2 fields
    private Spinner spinnerTipoDocumento;
    private TextInputLayout inputDescricaoDocumento, inputFormatoDocumento;

    // Confirmation labels (Step 3)
    private TextView labelNome, labelEndereco, labelFrequencia,
            labelLatitude, labelLongitude,
            labelDocTipo, labelDocDescricao;

    // Step 4 fields
    private Spinner spinnerPagamentoTipo;
    private EditText inputDetalhesPagamento;

    // Map
    private MapView mapView;
    private GoogleMap googleMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_ATTACH_DOC = 2001;


    ImageButton btnCarregarFoto;
    ImageView ivPreview;
    ImageButton btnAnexarDoc;
    TextView tvDocName;
    private ImageView stepIcon1, stepIcon2, stepIcon3, stepIcon4;


    private static final int REQUEST_LOCATION_PERM = 1002;

    private FusedLocationProviderClient fusedLocationClient;
    TextView tvIvaAmount;
    TextView tvTotalTaxAmount;
    EditText inputDataInicio;
    EditText inputDataFim;
    Spinner spinnerMetodoPagamento;

    private TextView tvProofTaxa,tvTaxaAmount,
            tvProofIva,
            tvProofTotalPago,
            tvProofMetodo,
            tvProofData,
            tvProofTxnId,
            tvProofNome,
            tvProofEndereco,
            tvProofFrequencia,
            tvProofReference,
            tvProofStartDate,
            tvProofEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.space_reservation_layout);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        stepIcon1 = findViewById(R.id.stepIcon1);
        stepIcon2 = findViewById(R.id.stepIcon2);
        stepIcon3 = findViewById(R.id.stepIcon3);
        stepIcon4 = findViewById(R.id.stepIcon4);


        btnCarregarFoto = findViewById(R.id.btnCarregarFoto);
        ivPreview = findViewById(R.id.ivFotoPreview);

        btnAnexarDoc = findViewById(R.id.btnAnexarDocumento);
        tvDocName = findViewById(R.id.tvNomeDocumentoAnexado);

        // ViewFlipper + nav buttons
        formSteps = findViewById(R.id.formSteps);
        btnAnterior = findViewById(R.id.btnAnterior);
        btnProximo = findViewById(R.id.btnProximo);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        // Step 1 → Detalhes
        inputNome = findViewById(R.id.inputNome);
        inputEndereco = findViewById(R.id.inputEndereco);
        inputLatitude = findViewById(R.id.inputLatitude);
        inputLongitude = findViewById(R.id.inputLongitude);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        ArrayAdapter<String> adapterFreq = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Mensal", "Trimestral", "Anual"}
        );
        adapterFreq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapterFreq);

        // Step 2 → Documentos
        spinnerTipoDocumento = findViewById(R.id.spinnerTipoDocumento);
        inputDescricaoDocumento = findViewById(R.id.inputDescricaoDocumento);
        ArrayAdapter<String> adapterDoc = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"BI", "Passaporte", "Declaração de residência", "Outro"}
        );
        adapterDoc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDocumento.setAdapter(adapterDoc);

        // Step 3 → Confirmação labels
        labelNome = findViewById(R.id.labelNome);
        labelEndereco = findViewById(R.id.labelEndereco);
        labelFrequencia = findViewById(R.id.labelFrequencia);
        labelDocTipo = findViewById(R.id.labelTipoDocumento);


        // MapView setup
        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = (savedInstanceState != null)
                ? savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
                : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        // Navigate backwards
        btnAnterior.setOnClickListener(v -> {
            if (stepIndex > 0) {
                stepIndex--;
                formSteps.setDisplayedChild(stepIndex);
                updateStepIndicator();
            }
        });

        // Navigate forwards
        btnProximo.setOnClickListener(v -> {
            // Before showing the Confirmação screen, fill in the labels
            if (stepIndex == 1) {
                preencherConfirmacao();
            }
            // Advance if there are more steps
            if (stepIndex < formSteps.getChildCount() - 1) {
                stepIndex++;
                formSteps.setDisplayedChild(stepIndex);
                updateStepIndicator();
            } else {

                // aqui, ao tocar “Concluir”, você pode finalizar a Activity ou submeter o form
                finish();

            }
        });
        updateStepIndicator();


         inputDataInicio = findViewById(R.id.inputDataInicio);
         inputDataFim = findViewById(R.id.inputDataFim);

        View.OnClickListener dateClickListener = v -> {
            final EditText target = (EditText) v;
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                day, month + 1, year);
                        target.setText(formatted);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        };

        inputDataInicio.setOnClickListener(dateClickListener);
        inputDataFim.setOnClickListener(dateClickListener);

        Calendar today = Calendar.getInstance();
        Calendar nextMonth = (Calendar) today.clone();
        nextMonth.add(Calendar.MONTH, 1);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        inputDataInicio.setText(sdf.format(today.getTime()));
        inputDataFim.setText(sdf.format(nextMonth.getTime()));

        tvProofTaxa = findViewById(R.id.tvProofTaxa);
        tvProofIva = findViewById(R.id.tvProoIva);
        tvProofTotalPago = findViewById(R.id.tvProofTotalPago);
        tvProofMetodo = findViewById(R.id.tvProofMetodo);
        tvProofData = findViewById(R.id.tvProofData);
        tvProofTxnId = findViewById(R.id.tvProofTxnId);
        tvProofNome = findViewById(R.id.tvProofNome);
        tvProofEndereco = findViewById(R.id.tvProofEndereco);
        tvProofFrequencia = findViewById(R.id.tvProofFrequencia);
        tvProofReference = findViewById(R.id.tvProofReference);
        tvProofStartDate = findViewById(R.id.tvProofStartDate);
        tvProofEndDate = findViewById(R.id.tvProofEndDate);


        labelEndereco = findViewById(R.id.labelEndereco);

       // btnExportPdf.setOnClickListener(v -> exportProofToPdf());


        btnCarregarFoto.setOnClickListener(v -> {
            Intent pickIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            pickIntent.setType("image/*");
            startActivityForResult(pickIntent, REQUEST_PICK_IMAGE);
        });


        btnAnexarDoc.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_ATTACH_DOC);
        });

         spinnerMetodoPagamento = findViewById(R.id.spinnerMetodoPagamento);

        String[] paymentMethods = new String[]{"Mpesa", "Emola", "Banco"};

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                paymentMethods
        );

// 4. Specify the layout to use when the list of choices appears
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerMetodoPagamento.setAdapter(paymentAdapter);
         tvTaxaAmount = findViewById(R.id.tvTaxaAmount);

        // generate random between min (inclusive) and max (inclusive)
        int min = 2000;
        int max = 100000;
        Random rnd = new Random();
        int randomTaxa = rnd.nextInt(max - min + 1) + min;

        // format with thousand separators, prefix currency code
        String formatted = NumberFormat
                .getNumberInstance(Locale.getDefault())
                .format(randomTaxa);
        tvTaxaAmount.setText("MZN " + formatted);
         tvIvaAmount      = findViewById(R.id.tvIvaAmount);
         tvTotalTaxAmount = findViewById(R.id.tvTotalTaxAmount);

        double iva   = randomTaxa * 0.16;
        double total = randomTaxa + iva;

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        String ivaStr   = "MZN " + nf.format(iva);
        String totalStr = "MZN " + nf.format(total);

        tvIvaAmount.setText(ivaStr);
        tvTotalTaxAmount.setText(totalStr);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 1) Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERM
            );
        } else {
            fetchAndFillLocation();
        }

    }


    private void preencherConfirmacao() {
        labelNome.setText("Nome: " + inputNome.getEditText().getText());
        labelEndereco.setText("Endereço: " + inputEndereco.getText());
        labelFrequencia.setText("Frequência: " + spinnerFrequency.getSelectedItem());
        labelDocTipo.setText("Tipo de Documento: " + spinnerTipoDocumento.getSelectedItem());

        updateMap();
    }

    private void updateMap() {
        if (googleMap != null) {
            googleMap.clear();
            try {
                double lat = Double.parseDouble(inputLatitude.getText().toString()),
                        lng = Double.parseDouble(inputLongitude.getText().toString());
                LatLng location = new LatLng(lat, lng);
                googleMap.addMarker(new MarkerOptions().position(location).title("Localização"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
            } catch (Exception e) {
                Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            ivPreview.setImageURI(imageUri);
            ivPreview.setVisibility(View.VISIBLE);
        }

        if (requestCode == REQUEST_ATTACH_DOC && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String name = uri.getLastPathSegment(); // or query DisplayName via ContentResolver
            tvDocName.setText(name);
        }
    }

    private void updateStepIndicator() {
        // array para facilitar
        ImageView[] icons = {stepIcon1, stepIcon2, stepIcon3, stepIcon4};
        int lastIndex = icons.length - 1;

        for (int i = 0; i < icons.length; i++) {
            if (i <= stepIndex) {
                // passos concluídos ou atual: círculo verde + check
                icons[i].setBackgroundResource(R.drawable.circle_green);
                icons[i].setImageResource(R.drawable.ic_baseline_check_24);
            } else {
                // passos futuros: círculo cinza + ícone padrão
                icons[i].setBackgroundResource(R.drawable.circle_gray);
                // se quiser trocar o src por outro ícone, faça aqui;
                // por simplicidade mantemos o mesmo:
                icons[i].setImageResource(R.drawable.ic_baseline_check_24);
            }
        }
        btnAnterior.setVisibility((stepIndex == 0 || stepIndex == lastIndex)
                ? View.GONE : View.VISIBLE);

        // change “Próximo” → “Concluir”
        btnProximo.setText(stepIndex == lastIndex ? "Concluir" : "Próximo");

        // only show Export button on the very last screen
        btnExportPdf.setVisibility(stepIndex == lastIndex
                ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERM &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAndFillLocation();
        } else {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAndFillLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, (Location location) -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        inputLatitude.setText(String.format(Locale.getDefault(), "%.6f", lat));
                        inputLongitude.setText(String.format(Locale.getDefault(), "%.6f", lng));
                    } else {
                        Toast.makeText(this, "Não foi possível obter localização", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao obter localização: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }


    private void populateProofStep() {
        // copy from your Taxa table
        tvProofTaxa.setText(tvTaxaAmount.getText());
        tvProofIva.setText(tvIvaAmount.getText());
        tvProofTotalPago.setText(tvTotalTaxAmount.getText());

        // from spinner / datepickers / transaction ID field
        tvProofMetodo.setText(spinnerMetodoPagamento.getSelectedItem().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvProofData.setText(sdf.format(new Date()));
        tvProofTxnId.setText(IdGenerator.randomAlphanumeric(10));

        // from your summary labels
        tvProofNome.setText(labelNome.getText().toString());
        tvProofEndereco.setText(labelEndereco.getText().toString().replace("Endereço: ", ""));
       // tvProofFrequencia.setText(labelExecutor.getText().toString().replace("Executor: ", ""));

        tvProofReference.setText(IdGenerator.randomAlphanumeric(10));

        // start/end dates
        tvProofStartDate.setText(inputDataInicio.getText());
        tvProofEndDate.setText(inputDataFim.getText());
    }

/*
    private void exportProofToPdf() {
        // 1) Measure & layout the view
        int specW = View.MeasureSpec.makeMeasureSpec(
                scrollProof.getWidth(), View.MeasureSpec.EXACTLY);
        int specH = View.MeasureSpec.makeMeasureSpec(
                0, View.MeasureSpec.UNSPECIFIED);
        scrollProof.measure(specW, specH);
        scrollProof.layout(0, 0,
                scrollProof.getMeasuredWidth(),
                scrollProof.getMeasuredHeight());

        // 2) Create the PDF document
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                scrollProof.getMeasuredWidth(),
                scrollProof.getMeasuredHeight(),
                1
        ).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // 3) Draw the view onto the PDF page
        scrollProof.draw(canvas);
        document.finishPage(page);

        // 4) Write the PDF to a file
        String filename = "comprovativo.pdf";
        File file = new File(getExternalFilesDir(null), filename);
        try (FileOutputStream out = new FileOutputStream(file)) {
            document.writeTo(out);
            Toast.makeText(this,
                    "PDF salvo em:\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Erro ao gerar PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }

*/


}

class IdGenerator {
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "abcdefghijklmnopqrstuvwxyz"
            + "0123456789";
    private static final SecureRandom rnd = new SecureRandom();

    public static String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = rnd.nextInt(ALPHANUM.length());
            sb.append(ALPHANUM.charAt(idx));
        }
        return sb.toString();
    }
}


