package com.example.asarma.njrails;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by asarma on 11/3/2017.
 */
/*
<tr onclick="javascript:window.open('train_stops.aspx?sid=NP&amp;train=3915');" style="color:white;background-color:red;overflow:hidden;">
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="left" valign="middle" style="font-family:Arial;width: 10%;overflow:hidden;white-Space:nowrap;"> 8:57 </td>
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="left" valign="middle" style="font-family:Arial;width: 45%;overflow:hidden;white-Space:nowrap;">Trenton&nbsp;✈</td>
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="center" valign="middle" style="font-family:Arial;width: 10%;overflow:hidden;white-Space:nowrap;">4</td>
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="left" valign="middle" style="font-family:Arial;width: 10%;overflow:hidden;white-Space:nowrap;">Northeast Corrdr</td>
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="right" valign="middle" style="font-family:Arial;width: 10%;overflow:hidden;white-Space:nowrap;">3915</td>
11-03 08:54:57.811 21940-21958/? I/System.out:   <td align="right" valign="middle" style="font-family:Arial;width: 15%;overflow:hidden;white-Space:nowrap;">in 11 Min</td>
11-03 08:54:57.811 21940-21958/? I/System.out:  </tr>
*/
public class DownloadFilesTask extends AsyncTask<String, Integer, Long> {

    MainActivityFragment parent;
    View view;
    public DownloadFilesTask(View view, MainActivityFragment parent)
    {
        this.parent = parent;
        this.view = view;
    }
    ArrayList<HashMap<String, Object>> result= new ArrayList<>();
    protected Long doInBackground(String... codes) {
        ArrayList<HashMap<String, Object>> result =  new ArrayList<HashMap<String, Object>>();
        String station = codes[1];

        try {
            Connection  conn =  Jsoup.connect("http://dv.njtransit.com/mobile/tid-mobile.aspx?sid="+ station).timeout(3000);
            Document doc = conn.get();
            Element table = doc.getElementById("GridView1");
            Node node = (Node) table;
            List<Node> child = node.childNodes().get(1).childNodes();
            // discard the frist 3
            System.out.println("child ===================== Size:" + child.size());
            for (int i = 3; i < child.size(); i++) {
                Node tr = child.get(i);
                List<Node> td = tr.childNodes();
                if (td.size()< 4 ) {
                    continue;
                }
                //System.out.println("td=" + td);

                HashMap<String, Object> data = new HashMap<>();
                String time = ((Element)td.get(1)).html().toString();
                String to =  ((Element)td.get(3)).html().toString();
                String track = ((Element)td.get(5)).html().toString();
                String line = ((Element)td.get(7)).html().toString();
                String train = ((Element)td.get(9)).html().toString();
                String status =  ((Element)td.get(11)).html().toString();;
                data.put("time", time);
                data.put("to", to);
                data.put("track", track);
                data.put("line", line);
                data.put("status", status);
                data.put("train", train);
                data.put("station", station);
                result.add(data);
                System.out.println("Mege: " + time + " track:" + track + "  train:" + train + " to:" + to + " status:" + status );
            }
           // System.out.println(table);
           // System.out.println("all done ..  =====================" + table);

            this.result = result;
            return new Long(0);
            // send the data to the main thread
        }
        catch (Exception e) {
            e.printStackTrace();

        }
        return new Long(-1);
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Long xx) {
        parent.updateAdapter(view, result);
        if( xx == -1 ){
            Toast.makeText(parent.getContext(), "Connection timed out for status", Toast.LENGTH_LONG).show();;
        }
    }
}
