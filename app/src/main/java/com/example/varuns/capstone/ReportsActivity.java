package com.example.varuns.capstone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.varuns.capstone.model.Artisan;
import com.example.varuns.capstone.model.Report;
import com.example.varuns.capstone.model.ReportDate;
import com.example.varuns.capstone.model.SoldItem;
import com.example.varuns.capstone.services.ApiService;
import com.example.varuns.capstone.services.DateUtil;
import com.example.varuns.capstone.services.RestfulResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {

    private GraphView graph;
    private ListView reportList;
    private ListView soldItemList;
    private Button backButton;
    private static ReportsActivity.ReportAdapter reportAdapterGlobal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        reportList = (ListView) findViewById(R.id.reportList);
        soldItemList = (ListView) findViewById(R.id.soldItemList);
        backButton = findViewById(R.id.back_to_report);
        backButton.setVisibility(View.GONE);
        soldItemList.setVisibility(View.GONE);

        reportList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                ReportDate reportDate = (ReportDate) parent.getAdapter().getItem(position);
                parent.setVisibility(View.GONE);
                backButton.setVisibility(View.VISIBLE);
                soldItemList.setVisibility(View.VISIBLE);
                ListView soldItemList = (ListView) findViewById(R.id.soldItemList);
                ReportsActivity.SoldItemAdapter soldItemAdapter = new ReportsActivity.SoldItemAdapter(reportDate);
                soldItemList.setAdapter(soldItemAdapter);


            }
        });

        // generate Dates
        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d2 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d3 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d4 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d5 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d6 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d7 = calendar.getTime();


        this.graph = (GraphView) findViewById(R.id.graph);

// you can directly pass Date objects to DataPoint-Constructor
// this will convert the Date to double via Date#getTime()
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(d1, 1),
                new DataPoint(d2, 5),
                new DataPoint(d3, 3),
                new DataPoint(d4, 1),
                new DataPoint(d5, 5),
                new DataPoint(d6, 3),
                new DataPoint(d7, 6)
        });

        graph.addSeries(series);

        graph.setTitle("Products Sold");

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);

        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d7.getTime());
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getGridLabelRenderer().setNumVerticalLabels(7);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(6);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.getGridLabelRenderer().setHumanRounding(false);

        setupBottomNavigationView();

        /*Spinner spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.values.fake_artisans, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);*/

        getArtisans();
    }

    public void goBackToReport(View view) {
        soldItemList.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        reportList.setVisibility(View.VISIBLE);
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_notifications);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Intent intent = new Intent(ReportsActivity.this, menu_activity.class);
                        startActivity(intent);
                        break;

                    case R.id.navigation_dashboard:
                        Intent intent1 = new Intent(ReportsActivity.this, Send_message.class);
                        startActivity(intent1);
                        break;

                    case R.id.navigation_notifications:
                        Intent intent2 = new Intent(ReportsActivity.this, ReportsActivity.class);
                        startActivity(intent2);
                        break;

                }
                return true;
            }
        });
    }

    public List<SoldItem> createGraphData(List<SoldItem> soldItems) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        Collections.sort(soldItems, new Comparator<SoldItem>() {
//            @Override
//            public int compare(SoldItem o1, SoldItem o2) {
//                return o1.getDateSold().getTime() < o2.getDateSold().getTime() ? -1 : 1;
//            }
//        });
        ReportsActivity.ReportAdapter reportAdapter = new ReportsActivity.ReportAdapter(soldItems);
        reportList.setAdapter(reportAdapter);
        reportAdapterGlobal = reportAdapter;

        List<Long> dateLongs = new ArrayList<>();
        for (SoldItem si : soldItems) {
            dateLongs.add(si.getDateSold().getTime());
        }
        DataPoint[] dataPointsArr = DateUtil.getDataPointsFromDates(dateLongs);


        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPointsArr);
        this.graph.addSeries(series);


        return soldItems;
    }

    public void getArtisans() {
        Call<RestfulResponse<List<SoldItem>>> call = ApiService.itemService().getSoldItemsByUserId("1");
        //handle the response
        call.enqueue(new Callback<RestfulResponse<List<SoldItem>>>() {
            @Override
            public void onResponse(Call<RestfulResponse<List<SoldItem>>> call, Response<RestfulResponse<List<SoldItem>>> response) {
                List<SoldItem> soldItems = response.body().getData();
                soldItems = createGraphData(soldItems);

                Toast.makeText(ReportsActivity.this, "success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<RestfulResponse<List<SoldItem>>> call, Throwable t) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Date d = new Date(System.currentTimeMillis());
                String s = gson.toJson(d);
            }
        });
    }


    private class SoldItemAdapter extends BaseAdapter {

        private ReportDate reportDate;

        public SoldItemAdapter(ReportDate reportDate) {
            this.reportDate = reportDate;
        }

        public void addSoldItem(SoldItem si) {
            reportDate.addSoldItem(si);
        }

        public List<SoldItem> getSoldItems() {
            return reportDate.getSoldItems();
        }

        public int getCount() {
            return reportDate.getSoldItems().size();
        }

        public SoldItem getItem(int i) {
            return reportDate.getSoldItems().get(i);

        }

        public long getItemId(int i) {
            return 0;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.report_date_layout, null);


            TextView soldItemDescTextView = (TextView) view.findViewById(R.id.soldItemDescription);
            TextView soldItemPriceTextView = (TextView) view.findViewById(R.id.soldItemCost);
            soldItemDescTextView.setText(reportDate.getSoldItems().get(i).getItemDescription());
            soldItemPriceTextView.setText("Price: " + reportDate.getSoldItems().get(i).getPrice().toString());



            return view;
        }
    }




    private class ReportAdapter extends BaseAdapter {

        private Report report;

        public ReportAdapter(List<SoldItem> soldItems) {
            report = new Report();
            report.addSoldItems(soldItems);
        }

        public void addSoldItem(SoldItem si) {
            report.addSoldItem(si);
        }

        public Collection<ReportDate> getReportDates() {
            return report.getReportDateMap().values();
        }

        public int getCount() {
            return report.getReportDateMap().size();
        }

        public ReportDate getItem(int i) {
            List<Long> dateTimes = new ArrayList<>(report.getReportDateMap().keySet());
            Collections.sort(dateTimes);
            return report.getReportDateMap().get(dateTimes.get(i));

        }

        public long getItemId(int i) {
            return 0;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.sold_item_report_layout, null);
            List<Long> dateTimes = new ArrayList<>(report.getReportDateMap().keySet());
            Collections.sort(dateTimes);

            TextView reportDate = (TextView) view.findViewById(R.id.reportDate);
            Date reportD = new Date(dateTimes.get(i));
            reportDate.setText(reportD.toString());


            return view;
        }
    }
}
