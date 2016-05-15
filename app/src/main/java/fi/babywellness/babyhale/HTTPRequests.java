package fi.babywellness.babyhale;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HTTPRequests {

    public interface HTTPRequestListener {
        void onFoundSingle(String title, String contentText, boolean addToBackStack);
        void onFoundMultiple(String[] names, String[] urls);
        void onHTTPFailure(boolean addToBackStack);
    }


    private static final String TAG = HTTPRequests.class.getSimpleName();

    private static RequestQueue queue = null;
    private static String cookie = null;

    public static void initQueueIfNeeded(Context context) {
        if (queue == null)
            queue = Volley.newRequestQueue(context);

        initCookieIfNeeded();
    }

    public static void initCookieIfNeeded() {
        if (cookie == null || cookie.isEmpty()) {
            requestWithName(null, "");
        }
    }

    public static void requestWithVnr(final HTTPRequestListener listener, final String vnr) {
        String url = "https://easiointi.kela.fi/laakekys_app/LaakekysApplication";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String name = getNameFromKelaResponse(response);

                if (name != null && !name.isEmpty()) {
                    requestWithName(listener, name);
                } else {
                    onFailure(listener, false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onFailure(listener, false);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("haenimi", "");
                params.put("haeaine", "");
                params.put("haeatc", "");
                params.put("haevnr", vnr);
                params.put("ok", "Hae");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                return params;
            }
        };

        request.setTag(TAG);

        queue.add(request);
    }

    public static void requestWithName(final HTTPRequestListener listener, final String name) {
        String url;
        if (Locale.getDefault().getCountry().equals("SE"))
            url = "http://laakeinfo.fi/Search.aspx?l=sv";
        else
            url = "http://laakeinfo.fi/Search.aspx";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.contains("<div class=\"listing\">")) {
                    List<List<String>> matches = getMatchList(response);

                    if (matches.get(0).size() > 0) {
                        if (listener != null) {
                            listener.onFoundMultiple(
                                    matches.get(0).toArray(new String[0]),
                                    matches.get(1).toArray(new String[0]));
                        }
                    } else {
                        if (listener != null)
                            onFailure(listener, false);
                    }
                } else if (response.contains("<div class=\"Section1\">")) {
                    if (listener != null)
                        listener.onFoundSingle(getTitle(response), getContentText(response), false);
                } else {
                    if (listener != null)
                        onFailure(listener, false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null)
                    onFailure(listener, false);
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("__EVENTTARGET", "");
                params.put("__EVENTARGUMENT", "");
                params.put("__VIEWSTATEGENERATOR", "BBBC20B8");

                if (Locale.getDefault().getCountry().equals("SE")) {
                    params.put("__VIEWSTATE", "/wEPDwUKMTc2NzY1NjI3Mg9kFgJmD2QWBAIDD2QWAgIFDxYCHgVjbGFzcwUGYWN0aXZlZAIFD2QWBGYPFgIfAAUJZnJtc2VhcmNoZAICDw8WBh4IQ3NzQ2xhc3MFA2J0bh4EVGV4dAUEU8OWSx4EXyFTQgICZGRkxZNjuqwB+Bi8Hv7v2yAq2dvHgIk=");
                    params.put("__EVENTVALIDATION", "/wEWBAKJ/aqcDALQkcCMBAL43fjZDALY7fiNCqJR2mEmEc9WcU1qkmqdtHZ2YgC4");
                    params.put("Search1:hdLanguageId", "4");
                } else {
                    params.put("__VIEWSTATE", "/wEPDwUKMTc2NzY1NjI3Mg9kFgJmD2QWBAIDD2QWAgIFDxYCHgVjbGFzcwUGYWN0aXZlZAIFD2QWBGYPFgIfAAUJZnJtc2VhcmNoZAICDw8WBh4IQ3NzQ2xhc3MFA2J0bh4EVGV4dAUDSEFFHgRfIVNCAgJkZGQ2BqYJzINvRHYkGXr7+fwtg0L8cQ==");
                    params.put("__EVENTVALIDATION", "/wEWBALa8vLKCALQkcCMBAL43fjZDALY7fiNCoNRBovCyGbCM+mhYEACTRYyyeuF");
                    params.put("Search1:hdLanguageId", "1");
                }

                params.put("Search1:txtSearch", name);
                params.put("Content-Type", "text/html; charset=utf-8");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Host", "www.laakeinfo.fi");
                params.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
                params.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                params.put("Accept-Language", "fi-FI,fi;q=0.8,en-US;q=0.5,en;q=0.3");
                params.put("Accept-Encoding", "gzip, deflate");
                params.put("Referer", "http://www.laakeinfo.fi/Search.aspx");

                if (cookie != null && !cookie.isEmpty())
                    params.put("Cookie", cookie);

                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String setCookie = response.headers.get("Set-Cookie");

                if ((cookie == null || cookie.isEmpty() && (setCookie != null && !setCookie.isEmpty()))) {
                    cookie = setCookie;
                }

                return super.parseNetworkResponse(response);
            }
        };

        request.setTag(TAG);

        queue.add(request);
    }

    public static void requestWithUrl(final HTTPRequestListener listener, final String url) {
        final StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                listener.onFoundSingle(getTitle(response), getContentText(response), true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onFailure(listener, true);
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Host", "www.laakeinfo.fi");
                params.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
                params.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                params.put("Accept-Language", "fi-FI,fi;q=0.8,en-US;q=0.5,en;q=0.3");
                params.put("Accept-Encoding", "gzip, deflate");
                params.put("Connection", "keep-alive");

                if (cookie != null && !cookie.isEmpty())
                    params.put("Cookie", cookie);

                return params;
            }
        };

        request.setTag(TAG);

        queue.add(request);
    }

    public static void cancelAll() {
        queue.cancelAll(TAG);
    }

    private static String getNameFromKelaResponse(String response) {
        String name = null;

        try {
            StringReader sr = new StringReader(response);
            BufferedReader br = new BufferedReader(sr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("<td class=\"pieni\" valign=\"top\">")) {
                    name = line.substring(line.indexOf("<b>") + 3);
                    name = name.substring(0, name.indexOf("</b>"));
                }
            }
        } catch (IOException ioe) {
        }

        return name;
    }

    private static List<List<String>> getMatchList(String response) {
        List<String> names = new ArrayList<>();
        List<String> urls = new ArrayList<>();

        Document document = Jsoup.parse(response);

        String baseUrl = "http://www.laakeinfo.fi";
        Element listElement;
        Node node;

        Elements elements = document.getElementsByAttributeValue("class", "listing");
        if (elements.size() > 0) {
            elements = elements.get(0).getElementsByTag("ul");
            if (elements.size() > 0)
                elements = elements.get(0).children();
        }

        for (int i = 0; i < elements.size(); i++) {
            listElement = elements.get(i);

            if (listElement.tagName().equals("li")) {
                for (int j = 0; j < listElement.childNodes().size(); j++) {
                    node = listElement.childNodes().get(j);

                    if (node instanceof Element && node.nodeName().equals("a")) {
                        names.add(((Element) node).ownText());
                        urls.add(baseUrl.concat(node.attr("href")));
                    }
                }
            }
        }

        List<List<String>> results = new ArrayList<>();
        results.add(names);
        results.add(urls);

        return results;
    }

    private static String getTitle(String response) {
        Document document = Jsoup.parse(response);
        String text = "";
        if (document != null) {
            Elements elements = document.getElementsByTag("h1");
            if (elements != null && elements.size() > 0)
                text = elements.get(0).ownText();
        }
        return text;
    }

    private static String getContentText(String response) {
        Document document = Jsoup.parse(response);
        Elements elements = new Elements();
        if (document != null) {
            elements = document.getElementsByAttributeValue("class", "Section1");
            if (elements != null && elements.size() > 0)
                elements.get(0).children();
        }
        return elements.toString();
    }

    private static void onFailure(HTTPRequestListener listener, boolean addtoBackStack) {
        listener.onHTTPFailure(addtoBackStack);
        initCookieIfNeeded();
    }
}
