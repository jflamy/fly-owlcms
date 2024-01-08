package app.owlcms.fly.flydata;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import org.slf4j.LoggerFactory;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;

import app.owlcms.fly.Main;
import ch.qos.logback.classic.Logger;

public class GeoLocator {

	private static Logger logger = (Logger)LoggerFactory.getLogger(GeoLocator.class);
	private static InputStream ds;
	private static DatabaseReader geoDatabaseReader;

	static {
		ds = Main.class.getResourceAsStream("/GeoLite2/GeoLite2-City.mmdb");
		try {
			geoDatabaseReader = new DatabaseReader.Builder(ds).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static EarthLocation locate(String ipAddressString) {
		try {
			logger.warn("ipAddressString {}",ipAddressString);
			if (ipAddressString.contentEquals("[0:0:0:0:0:0:0:1]") || ipAddressString.contentEquals("127.0.0.1")) {
				ipAddressString = "107.171.217.85";
			}
			CityResponse val = geoDatabaseReader.city(InetAddress.getByName(ipAddressString));
			Location loc = val.getLocation();
			return new EarthLocation(val.getCity().getName(), "", loc.getLatitude(), loc.getLongitude());
		} catch (IOException | GeoIp2Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
