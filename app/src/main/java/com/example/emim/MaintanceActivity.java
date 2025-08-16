package com.example.emim;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emim.controller.Coordinate;
import com.example.emim.controller.CoordinatesAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MaintanceActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ViewFlipper formSteps;
    private int stepIndex = 0;

    private Button btnAnterior, btnProximo, btnExportPdf;

    // Step 1 fields
    private TextInputEditText inputEndereco, inputLocalManutencao;
    private TextInputLayout inputLatitude, inputLongitude;
    private Spinner spinnerExecutor, spinnerMetodoPagamento;

    private TextInputLayout inputNome;

    // Step 2 fields
    private Spinner spinnerTipoDocumento;
    private TextInputLayout inputDescricaoDocumento, inputFormatoDocumento, tvLandingDate;

    // Confirmation labels (Step 3)
    private TextView labelNome, labelEndereco, labelFrequencia, labelExecutor, tvTaxaAmount, tvTotalTaxAmount, tvIvaAmount;


    // Map
    private MapView mapView;
    private GoogleMap googleMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_ATTACH_DOC = 2001;


    ImageButton btnCarregarFoto;
    ImageView ivPreview;
    ImageButton btnAnexarDoc;
    TextView tvDocName, labelLocalManutencao;
    private ImageView stepIcon1, stepIcon2, stepIcon3, stepIcon4, stepIcon5;


    private static final int REQUEST_LOCATION_PERM = 1002;

    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView rvCoords;
    private CoordinatesAdapter adapter;

    private final List<Coordinate> coordinateList = new ArrayList<>();
    private Polyline currentPolyline;
    private Marker startMarker, endMarker;
    private View scrollProof;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    TextInputEditText inputDataInicio;
    TextInputEditText inputDataFim;

    private TextView tvProofTaxa,
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
        setContentView(R.layout.maintanance_layout);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        stepIcon1 = findViewById(R.id.stepIcon1);
        stepIcon2 = findViewById(R.id.stepIcon2);
        stepIcon3 = findViewById(R.id.stepIcon3);
        stepIcon4 = findViewById(R.id.stepIcon4);
        stepIcon5 = findViewById(R.id.stepIcon5);
        scrollProof = findViewById(R.id.scrollProof);


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
        inputLocalManutencao = findViewById(R.id.etlocalManutencaol);
        inputEndereco = findViewById(R.id.inputEndereco);
        inputLatitude = findViewById(R.id.inputLatitude);
        inputLongitude = findViewById(R.id.inputLongitude);


        inputDataInicio = findViewById(R.id.inputDataInicio);
        inputDataFim = findViewById(R.id.inputDataFim);

        spinnerExecutor = findViewById(R.id.spinnerExecutor);
        ArrayAdapter<String> adapterFreq = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Próprio", "Emim", "Empreteiros"}
        );
        adapterFreq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExecutor.setAdapter(adapterFreq);

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

        labelEndereco = findViewById(R.id.labelEndereco);
        labelFrequencia = findViewById(R.id.labelFrequencia);
        labelLocalManutencao = findViewById(R.id.labelLocalManutencao);
        labelExecutor = findViewById(R.id.labelExecutor);


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

            if (stepIndex == 1) {
                preencherConfirmacao();
            }
            if (stepIndex == 3) {
                populateProofStep();
            }
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

        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMetodoPagamento.setAdapter(paymentAdapter);
        tvTaxaAmount = findViewById(R.id.tvTaxaAmount);

        int min = 2000;
        int max = 100000;
        Random rnd = new Random();
        int randomTaxa = rnd.nextInt(max - min + 1) + min;

        String formatted = NumberFormat
                .getNumberInstance(Locale.getDefault())
                .format(randomTaxa);
        tvTaxaAmount.setText("MZN " + formatted);

        tvIvaAmount = findViewById(R.id.tvIvaAmount);
        tvTotalTaxAmount = findViewById(R.id.tvTotalTaxAmount);

        double iva = randomTaxa * 0.16;
        double total = randomTaxa + iva;

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        String ivaStr = "MZN " + nf.format(iva);
        String totalStr = "MZN " + nf.format(total);
        tvIvaAmount.setText(ivaStr);
        tvTotalTaxAmount.setText(totalStr);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        Button btnAdd = findViewById(R.id.btnAddCoordinate);
        rvCoords = findViewById(R.id.rvCoordinates);

        adapter = new CoordinatesAdapter(new ArrayList<>(), pos -> adapter.remove(pos));
        rvCoords.setLayoutManager(new LinearLayoutManager(this));
        rvCoords.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String lat = inputLatitude.getEditText().getText().toString().trim();
            String lon = inputLongitude.getEditText().getText().toString().trim();
            if (lat.isEmpty() || lon.isEmpty()) {
                Toast.makeText(this, "Preencha latitude e longitude", Toast.LENGTH_SHORT).show();
                return;
            }
            Coordinate coord = new Coordinate(lat, lon);
            adapter.add(coord);                // show in RecyclerView
            coordinateList.add(coord);
            inputLatitude.getEditText().setText("");
            inputLongitude.getEditText().setText("");
            drawPolyline();
        });

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


        labelLocalManutencao = findViewById(R.id.labelLocalManutencao);
        labelEndereco = findViewById(R.id.labelEndereco);
        labelExecutor = findViewById(R.id.labelExecutor);

        btnExportPdf.setOnClickListener(v -> exportProofToPdf());

    }


    private void preencherConfirmacao() {

        // Endereço
        labelEndereco.setText(inputEndereco.getText());

        // Local de Manutenção
        labelLocalManutencao.setText(inputLocalManutencao.getText().toString()
        );

        // Executor
        labelExecutor.setText(spinnerExecutor.getSelectedItem().toString()
        );

    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        drawPolyline();
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
        ImageView[] icons = {stepIcon1, stepIcon2, stepIcon3, stepIcon4, stepIcon5};
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, (Location location) -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        inputLatitude.getEditText().setText(String.format(Locale.getDefault(), "%.6f", lat));
                        inputLongitude.getEditText().setText(String.format(Locale.getDefault(), "%.6f", lng));
                    } else {
                        Toast.makeText(this, "Não foi possível obter localização", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao obter localização: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void drawPolyline() {
        try {
            if (googleMap == null || coordinateList.size() < 2) return;

            // Build origin, destination, and waypoints
            LatLng origin = new LatLng(
                    Double.parseDouble(coordinateList.get(0).latitude),
                    Double.parseDouble(coordinateList.get(0).longitude)
            );
            LatLng destination = new LatLng(
                    Double.parseDouble(coordinateList.get(coordinateList.size() - 1).latitude),
                    Double.parseDouble(coordinateList.get(coordinateList.size() - 1).longitude)
            );

            StringBuilder waypoints = new StringBuilder();
            // skip first & last
            for (int i = 1; i < coordinateList.size() - 1; i++) {
                Coordinate c = coordinateList.get(i);
                if (waypoints.length() > 0) waypoints.append("|");
                waypoints.append(c.latitude).append(",").append(c.longitude);
            }

            String API_KEY = "AIzaSyAMRW9dUZnP7IP8LDjVU7swa8-ixrrHa8I";
            String url = "https://maps.googleapis.com/maps/api/directions/json"
                    + "?origin=" + origin.latitude + "," + origin.longitude
                    + "&destination=" + destination.latitude + "," + destination.longitude
                    + (waypoints.length() > 0 ? "&waypoints=" + URLEncoder.encode(waypoints.toString(), "UTF-8") : "")
                    + "&key=" + API_KEY;

            Request request = new Request.Builder().url(url).build();
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(MaintanceActivity.this, "Directions failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response res) throws IOException {
                    if (!res.isSuccessful()) {
                        onFailure(call, new IOException("HTTP " + res.code()));
                        return;
                    }
                    String body = res.body().string();
                    // Parse “overview_polyline.points” via GSON or JSONObject
                    try {
                        JSONObject json = new JSONObject(body);
                        JSONArray routes = json.getJSONArray("routes");
                        if (routes.length() == 0) throw new JSONException("no routes");
                        String polyline = routes
                                .getJSONObject(0)
                                .getJSONObject("overview_polyline")
                                .getString("points");
                        List<LatLng> pts = PolyUtil.decode(polyline);

                        runOnUiThread(() -> {
                            // clear old
                            if (currentPolyline != null) currentPolyline.remove();
                            if (startMarker != null) startMarker.remove();
                            if (endMarker != null) endMarker.remove();

                            // draw new
                            currentPolyline = googleMap.addPolyline(new PolylineOptions()
                                    .addAll(pts)
                                    .width(8)
                                    .color(Color.BLUE)
                            );
                            // markers
                            startMarker = googleMap.addMarker(new MarkerOptions().position(origin).title("Início").icon(bitmapDescriptorFromVector(R.drawable.ic_baseline_my_location_24)));
                            endMarker = googleMap.addMarker(new MarkerOptions().position(destination).title("Fim").icon(bitmapDescriptorFromVector(R.drawable.ic_baseline_my_location_24)));

                            // zoom to bounds
                            LatLngBounds.Builder b = new LatLngBounds.Builder();
                            for (LatLng p : pts) b.include(p);
                            googleMap.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(b.build(), 100)
                            );
                        });
                    } catch (JSONException e) {
                        onFailure(call, new IOException("Parse error", e));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private BitmapDescriptor bitmapDescriptorFromVector(@DrawableRes int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        vectorDrawable.setBounds(0, 0,
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight()
        );
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
        tvProofNome.setText(labelLocalManutencao.getText().toString().replace("Local de manutenção: ", ""));
        tvProofEndereco.setText(labelEndereco.getText().toString().replace("Endereço: ", ""));
        tvProofFrequencia.setText(labelExecutor.getText().toString().replace("Executor: ", ""));

        tvProofReference.setText(IdGenerator.randomAlphanumeric(10));

        // start/end dates
        tvProofStartDate.setText(inputDataInicio.getText());
        tvProofEndDate.setText(inputDataFim.getText());
    }


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
}


 class IdGenerator_ {
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

