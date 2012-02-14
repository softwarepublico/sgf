package br.gov.ce.fortaleza.cti.sgf.util;

import java.sql.SQLException;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.postgresql.util.PGobject;

public class GeometryConverter implements Converter {

	private static final long serialVersionUID = 5486361950235757194L;

	public Object convertDataValueToObjectValue(Object value, Session session) {
		Geometry result = null;
		if (value != null) {
			PGobject pgObject = (PGobject) value;
			try {
				result = PGgeometry.geomFromString(pgObject.getValue());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public Object convertObjectValueToDataValue(Object value, Session session) {
		PGgeometry result = new PGgeometry();
		if (value != null) {
			result.setGeometry((Geometry) value);
		}
		return result; 
	}

	public void initialize(DatabaseMapping databaseMapping, Session session) {
	}

	public boolean isMutable() {
		return false;
	}
}