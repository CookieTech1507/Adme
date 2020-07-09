package com.blz.cookietech.invoice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


public class Invoice extends AppCompatActivity{
    private static final String TAG = "Invoice";
    private Menu menuItem;
    private EditText customer_name;
    private EditText customer_phone;
    private EditText customer_email;
    private EditText customer_address;
    private EditText et_discount_amount;
    private Button btn_send_invoice;
    private ScrollView root_scrollView;
    private TextView invoice_total,total_amount;
    CustomerDetails detailsForServices;
    ArrayList<Services> serviceList;
    String receiver_email="";
    String service_id="";
    FirebaseFirestore db;
    double sumTotal = 0;
    boolean isEditable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        root_scrollView = findViewById(R.id.root_scrollView);

        detailsForServices = getIntent().getParcelableExtra("service_details");
        assert detailsForServices != null;
        receiver_email = detailsForServices.getCustomer_email();
        service_id = detailsForServices.getService_id();

        serviceList = getIntent().getParcelableArrayListExtra("service_list");
        assert serviceList != null;
        CreateInvoiceForService(detailsForServices,serviceList);

        String mode = getIntent().getStringExtra("mode");
        if(mode.equals("notEditable")){
            isEditable = false;
        }

        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEditable){
                    showAlert();
                } else {
                    onBackPressed();
                }
            }
        });

        btn_send_invoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                btn_send_invoice.setVisibility(View.GONE);
//                GeneratePdf();

                if(detailsForServices.getAppointment_id()!=null){
                    InvoiceItem invoiceItem = new InvoiceItem(detailsForServices, serviceList);

                    db = FirebaseFirestore.getInstance();
                    db.collection("Adme_Invoice_List").document(detailsForServices.getAppointment_id())
                            .set(invoiceItem)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "invoiceItem successfully written!");
                                    isEditable=false;
                                    onBackPressed();
//                                    Intent myIntent = new Intent(Invoice.this,Class.forName("com.example.adme.Activities.LandingActivity"));
//                                    startActivity(myIntent );
                                    db.collection("Adme_Appointment_list")
                                            .document(detailsForServices.getAppointment_id())
                                            .update("invoiceID", detailsForServices.getAppointment_id())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                    createNotification(
                                                            appointment.getService_provider_name() + " canceled your appointment",
                                                            FirebaseUtilClass.MODE_CLIENT + "",
                                                            appointment.getClint_ref() + "",
                                                            "Appointment canceled"
                                                    );

                                                    createNotification(
                                                            "You've canceled an appointment",
                                                            FirebaseUtilClass.MODE_SERVICE_PROVIDER + "",
                                                            appointment.getService_provider_ref() + "",
                                                            "Appointment canceled successful."
                                                    );
                                                    onBackPressed();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error updating document", e);
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
                }
            }
        });
    }
    public void createNotification(String text, String mode, String reference, String toastText){
        String newDocumentID = String.valueOf(Calendar.getInstance().getTimeInMillis());
        Notification notification = new Notification();
        notification.setSeen(false);
        notification.setTime(newDocumentID);
        notification.setText(text);
        notification.setMode(mode);
        notification.setType(FirebaseUtilClass.NOTIFICATION_APPOINTMENT_TYPE);
        notification.setReference(appointmentID);

        db.collection("Adme_User/"+ reference +"/notification_list")
                .document(newDocumentID)
                .set(notification)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Notification successfully written!");
                        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
    private void GeneratePdf() {

        View view = findViewById(R.id.root_scrollView);

        int totalHeight = root_scrollView.getChildAt(0).getHeight();
        int totalWidth = root_scrollView.getChildAt(0).getWidth();
        int btn_height = btn_send_invoice.getHeight();
        Bitmap bitmap = getBitmapFromView(view,totalHeight-btn_height,totalWidth);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float height = displaymetrics.heightPixels ;
        float width = displaymetrics.widthPixels ;

        int convertHeight = (int) height, convertWidth = (int) width;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(convertWidth, convertHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        canvas.drawPaint(paint);

        bitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHeight, true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0 , null);
        document.finishPage(page);

        File file = new File(getExternalCacheDir(),"invoice_"+service_id+".pdf");
        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            file.setReadable(true,false);
            SharePdf(file);
            btn_send_invoice.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    private  void SharePdf(File file) {

        Uri fileUri =  FileProvider.getUriForFile(Invoice.this, getApplicationContext().getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.putExtra(Intent.EXTRA_EMAIL,receiver_email);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Invoice for Service");
        intent.setType("application/pdf");
        //need to fix the bug if no shareable app found
        startActivity(Intent.createChooser(intent,"Share Invoice Via"));


    }



    private Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {
        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth,totalHeight , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    private void CreateInvoiceForService(CustomerDetails detailsForServices, ArrayList<Services> serviceList) {

        invoice_total = findViewById(R.id.invoice_total);
        total_amount = findViewById(R.id.total_amount);
        et_discount_amount = findViewById(R.id.et_discount_amount);
        TextView subtotal = findViewById(R.id.subtotal);
        TextView total_vat = findViewById(R.id.total_vat);
        TextView txt_vat = findViewById(R.id.txt_vat);
        btn_send_invoice = findViewById(R.id.btn_send_invoice);


        sumTotal = 0;

        for (Services services: serviceList){
            sumTotal += (services.getService_cost() * services.getService_quantity());
        }
        String subTotal = "$"+ sumTotal;
        subtotal.setText(subTotal);

        if (detailsForServices.getVat() == 0){
            txt_vat.setVisibility(View.GONE);
            total_vat.setVisibility(View.GONE);
            invoice_total.setText(subTotal);
            total_amount.setText(subTotal);
        } else {
            String vat = detailsForServices.getVat() + "%";
            total_vat.setText(vat);
            String totalWithVat = "$" + (sumTotal + ((sumTotal*detailsForServices.getVat())/100));
            invoice_total.setText(totalWithVat);
            total_amount.setText(totalWithVat);
        }

        if (detailsForServices.getDiscount() > 0){
            String totalWithDiscount = "$" + (sumTotal - detailsForServices.getDiscount());
            invoice_total.setText(totalWithDiscount);
            total_amount.setText(totalWithDiscount);
            et_discount_amount.setText("$"+detailsForServices.getDiscount());
        }



        TextView issue_date = findViewById(R.id.issue_date);
        issue_date.setText(detailsForServices.getDue_date());

        customer_name = findViewById(R.id.customer_name);
        customer_phone = findViewById(R.id.customer_phone);
        customer_email = findViewById(R.id.customer_email);
        customer_address = findViewById(R.id.customer_address);

        customer_name.setText(detailsForServices.getCustomer_name());
        customer_phone.setText(detailsForServices.getCustomer_phone());
        customer_email.setText(detailsForServices.getCustomer_email());
        customer_address.setText(detailsForServices.getCustomer_address());

        TextView service_provider = findViewById(R.id.service_provider);
        TextView service_category = findViewById(R.id.service_category);
        service_provider.setText(detailsForServices.getService_provider());
        service_category.setText(detailsForServices.getService_category());

        RecyclerView service_details_rv = findViewById(R.id.service_details_rv);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Invoice.this);
        service_details_rv.setLayoutManager(layoutManager);
        service_details_rv.setHasFixedSize(true);
        ServiceDetailsAdapter adapter = new ServiceDetailsAdapter(serviceList);
        service_details_rv.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        menuItem = menu;
        if(!isEditable) {
            menuItem.getItem(0).setVisible(false);
            menuItem.getItem(1).setVisible(false);
            btn_send_invoice.setVisibility(View.GONE);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_edit){
            menuItem.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menuItem.getItem(0).setVisible(false);
            menuItem.getItem(1).setVisible(true);
            menuItem.getItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            EnableEdit();
            btn_send_invoice.setVisibility(View.GONE);
        } else if (item.getItemId() == R.id.menu_done){
            menuItem.getItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menuItem.getItem(1).setVisible(false);
            menuItem.getItem(0).setVisible(true);
            menuItem.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            DisableEdit();
            btn_send_invoice.setVisibility(View.VISIBLE);
            UpdateTotalMoney();
        }
        return true;
    }

    private void DisableEdit() {
        customer_name.setEnabled(false);
        customer_phone.setEnabled(false);
        customer_email.setEnabled(false);
        customer_address.setEnabled(false);
        et_discount_amount.setEnabled(false);
        customer_name.setBackgroundResource(0);
        customer_phone.setBackgroundResource(0);
        customer_email.setBackgroundResource(0);
        customer_address.setBackgroundResource(0);
        et_discount_amount.setBackgroundResource(0);
    }

    private void EnableEdit() {
        customer_name.setEnabled(true);
        customer_phone.setEnabled(true);
        customer_email.setEnabled(true);
        customer_address.setEnabled(true);
        et_discount_amount.setEnabled(true);
        et_discount_amount.setText(""+detailsForServices.getDiscount());
        customer_name.setBackground(getDrawable(R.drawable.bottom_border_grey));
        customer_phone.setBackground(getDrawable(R.drawable.bottom_border_grey));
        customer_email.setBackground(getDrawable(R.drawable.bottom_border_grey));
        customer_address.setBackground(getDrawable(R.drawable.bottom_border_grey));
        et_discount_amount.setBackground(getDrawable(R.drawable.bottom_border_grey));
    }

    @Override
    public void onBackPressed() {
        if(isEditable){
            showAlert();
        } else {
            finish();
        }
    }

    private void showAlert() {
        AlertDialog.Builder exitAlert = new AlertDialog.Builder(this);
        exitAlert.setTitle("Are you sure?")
                .setMessage("Do you want to exit? It will delete all your saved progress")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        exitAlert.show();
    }

    private void UpdateTotalMoney() {
        Double discount = Double.parseDouble(et_discount_amount.getText().toString());
        et_discount_amount.setText("$" + discount);
        String totalWithDiscount = "$" + (sumTotal - discount);
        invoice_total.setText(totalWithDiscount);
        total_amount.setText(totalWithDiscount);
    }


}
