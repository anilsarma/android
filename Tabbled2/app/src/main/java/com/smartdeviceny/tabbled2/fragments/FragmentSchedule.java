package com.smartdeviceny.tabbled2.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.smartdeviceny.tabbled2.MainActivity;
import com.smartdeviceny.tabbled2.R;
import com.smartdeviceny.tabbled2.SystemService;
import com.smartdeviceny.tabbled2.adapters.RecycleSheduleAdaptor;
import com.smartdeviceny.tabbled2.adapters.ServiceConnected;

import java.util.ArrayList;

public class FragmentSchedule extends Fragment implements ServiceConnected {
    RecycleSheduleAdaptor adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_njt_schedule, container, false);
        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = getActivity().findViewById(R.id.schedule_vision_scroll_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

        ArrayList<SystemService.Route> routes = new ArrayList<>();

        if( ((MainActivity)this.getActivity()).systemService  == null ) {
            Log.d("FRAG", "System Service i null ");
        }
        else {
            routes = ((MainActivity)this.getActivity()).systemService.getRoutes("New York Penn Station", "New Brunswick", null);
        }
        Log.d("FRAG", "onViewCreated");
        adapter = new RecycleSheduleAdaptor(getActivity(), routes);
        //adapter.setClickListener(getc);
        recyclerView.setAdapter(adapter);

//        SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.schedule_vision_scroll_view);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.schedule_vision_scroll_view);
//                WebView web = getActivity().findViewById(R.id.nj_map_view_layout);
//                web.reload();
//                swipeRefreshLayout.setRefreshing(false);
//            }
//        });

//        WebView web = getActivity().findViewById(R.id.nj_map_view_layout);
//        web.loadUrl("http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=NY");
//        web.getSettings().setBuiltInZoomControls(true);
//        web.getSettings().setLoadWithOverviewMode(true);
//        web.getSettings().setUseWideViewPort(true);
//
//        NestedScrollView scrollView = getActivity().findViewById(R.id.departure_vision_scroll_view);
//        //scrollView.getChildAt();
//        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.departure_vision_layout);
//                //LinearLayout llParent = findViewById(R.id.departure_vision_scroll);
//                swipeRefreshLayout.setEnabled(scrollY ==0); // enable only if at the top.
//            }
//        });

        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(getActivity().getApplicationContext(), "OnViewCreated", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onSystemServiceConnected(SystemService systemService) {
        RecyclerView recyclerView = getActivity().findViewById(R.id.schedule_vision_scroll_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ArrayList<SystemService.Route> routes = new ArrayList<>();

        if( ((MainActivity)this.getActivity()).systemService  == null ) {
            Log.d("FRAG", "System Service i null ");
        }
        else {
            routes = ((MainActivity)this.getActivity()).systemService.getRoutes("New York Penn Station", "New Brunswick", null);
        }
        adapter.updateRoutes(routes);
        adapter.notifyDataSetChanged();;
        Log.d("FRAG", "updated routes");
    }
}
