package com.chdc.qr.lib.db;

import com.chdc.qr.QRResources;
import org.apache.commons.dbcp2.BasicDataSource;

public class DataSource implements QRResources {
	private static final int CONN_POOL_SIZE = 5;

	private BasicDataSource bds = new BasicDataSource();

	private DataSource() {
		//Set database driver name
		bds.setDriverClassName(DRIVER_CLASS_NAME);
		//Set database url
		bds.setUrl(DB_URL);
		//Set database user
		bds.setUsername(DB_USER);
		//Set database password
		bds.setPassword(DB_PASSWORD);
		//Set the connection pool size
		bds.setInitialSize(CONN_POOL_SIZE);
	}

	private static class DataSourceHolder {
		private static final DataSource INSTANCE = new DataSource();
	}

	public static DataSource getInstance() {
		return DataSourceHolder.INSTANCE;
	}

	public BasicDataSource getBds() {
		return bds;
	}

	public void setBds(BasicDataSource bds) {
		this.bds = bds;
	}
}