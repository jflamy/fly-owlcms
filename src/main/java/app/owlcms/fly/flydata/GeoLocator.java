package app.owlcms.fly.flydata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import org.slf4j.LoggerFactory;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;

import ch.qos.logback.classic.Logger;

public class GeoLocator {

	private static Logger logger = (Logger) LoggerFactory.getLogger(GeoLocator.class);
	private static InputStream ds;
	private static DatabaseReader geoDatabaseReader = null;

	private static void init() {
		File mmdb = new File("GeoLite2-City.mmdb");
		try {
			if (mmdb.exists()) {
				DatabaseReader reader;
				reader = new DatabaseReader.Builder(mmdb).fileMode(com.maxmind.db.Reader.FileMode.MEMORY_MAPPED)
				        .withCache(new CHMCache()).build();
				setGeoDatabaseReader(reader);

			} else {
				ds = GeoLocator.class.getResourceAsStream("/GeoLite2/GeoLite2-City.mmdb");

				setGeoDatabaseReader(new DatabaseReader.Builder(ds).build());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static EarthLocation locate(String ipAddressString) {
		try {
			logger.warn("ipAddressString {}", ipAddressString);
			if (ipAddressString.contentEquals("[0:0:0:0:0:0:0:1]") || ipAddressString.contentEquals("127.0.0.1")) {
				ipAddressString = "107.171.217.85";
			}
			DatabaseReader geoDatabaseReader2 = getGeoDatabaseReader();
			if (geoDatabaseReader2 == null) {
				return null;
			}
			CityResponse val = geoDatabaseReader2.city(InetAddress.getByName(ipAddressString));
			Location loc = val.getLocation();
			return new EarthLocation(val.getCity().getName(), "", loc.getLatitude(), loc.getLongitude());
		} catch (IOException | GeoIp2Exception e) {
			logger.warn(e.getMessage());
		}
		return null;
	}

	private static DatabaseReader getGeoDatabaseReader() {
		if (geoDatabaseReader == null) {
			init();
		}
		return geoDatabaseReader;
	}

	private static void setGeoDatabaseReader(DatabaseReader geoDatabaseReader) {
		GeoLocator.geoDatabaseReader = geoDatabaseReader;
	}

}
