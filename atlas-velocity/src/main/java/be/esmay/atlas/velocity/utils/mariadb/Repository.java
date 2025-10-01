package be.esmay.atlas.velocity.utils.mariadb;

import com.craftmend.storm.Storm;
import com.craftmend.storm.api.enums.Where;
import lombok.SneakyThrows;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Repository is a class that handles all the database operations.
 * @author Mindgamesnl
 * @param <T>
 */
public final class Repository<T extends DataStore> {
	
	private Storm storm;
	private Class<? extends DataStore> type;
	private MySQLClient client;

	@SneakyThrows
	public void onCreate(Storm storm, Class<? extends DataStore> dataClass, MySQLClient client) {
		this.storm = storm;
		this.type = dataClass;
		this.client = client;
		
		storm.registerModel(dataClass.getConstructor().newInstance());
		storm.runMigrations();
	}

	@SneakyThrows
	public CompletableFuture<Collection<T>> values() {
		CompletableFuture<Collection<T>> completableFuture = new CompletableFuture<>();
		CompletableFuture.runAsync(() -> {
			try {
				Collection<T> collection = (Collection<T>) this.client.getStorm()
						.buildQuery(this.type)
						.execute()
						.join();

				completableFuture.complete(collection);
			} catch (Exception exception) {
				exception.printStackTrace();
				completableFuture.completeExceptionally(exception);
			}
		});

		return completableFuture;
	}

	@SneakyThrows
	public CompletableFuture<Collection<T>> valuesWhere(String row, Object value) {
		CompletableFuture<Collection<T>> completableFuture = new CompletableFuture<>();
		CompletableFuture.runAsync(() -> {
			try {
				Collection<T> collection = (Collection<T>) this.client.getStorm()
						.buildQuery(this.type)
						.where(row, Where.EQUAL, value)
						.execute()
						.join();

				completableFuture.complete(collection);
			} catch (Exception exception) {
				exception.printStackTrace();
				completableFuture.completeExceptionally(exception);
			}
		});

		return completableFuture;
	}

	@SneakyThrows
	public CompletableFuture<T> getWhere(String row, Object value) {
		CompletableFuture<T> completableFuture = new CompletableFuture<>();
		CompletableFuture.runAsync(() -> {
			try {
				Collection<T> collection = (Collection<T>) this.client.getStorm()
						.buildQuery(this.type)
						.where(row, Where.EQUAL, value)
						.limit(1)
						.execute()
						.join();

				completableFuture.complete(collection.stream().findFirst().orElse(null));
			} catch (Exception exception) {
				exception.printStackTrace();
				completableFuture.completeExceptionally(exception);
			}
		});

		return completableFuture;
	}

	@SneakyThrows
	public CompletableFuture<T> getWhereAnd(String row, Object value, String secondRow, Object secondValue) {
		CompletableFuture<T> completableFuture = new CompletableFuture<>();
		CompletableFuture.runAsync(() -> {
			try {
				Collection<T> collection = (Collection<T>) this.client.getStorm()
						.buildQuery(this.type)
						.where(row, Where.EQUAL, value)
						.where(secondRow, Where.EQUAL, secondValue)
						.limit(1)
						.execute()
						.join();

				completableFuture.complete(collection.stream().findFirst().orElse(null));
			} catch (Exception exception) {
				exception.printStackTrace();
				completableFuture.completeExceptionally(exception);
			}
		});

		return completableFuture;
	}

	@SneakyThrows
	public T getWhereSync(String row, Object value) {
		Collection<T> collection = (Collection<T>) this.client.getStorm()
				.buildQuery(this.type)
				.where(row, Where.EQUAL, value)
				.limit(1)
				.execute()
				.join();

		return collection.stream().findFirst().orElse(null);
	}

	@SneakyThrows
	public T getWhereAndSync(String row, Object value, String secondRow, Object secondValue) {
		Collection<T> collection = (Collection<T>) this.client.getStorm()
				.buildQuery(this.type)
				.where(row, Where.EQUAL, value)
				.where(secondRow, Where.EQUAL, secondValue)
				.limit(1)
				.execute()
				.join();

		return collection.stream().findFirst().orElse(null);
	}

	@SneakyThrows
	public CompletableFuture<Integer> save(T data) {
		CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
		CompletableFuture.runAsync(() -> {
			try {
				completableFuture.complete(this.storm.save(data));
			} catch (SQLException exception) {
				completableFuture.completeExceptionally(exception);
			}
		});

		return completableFuture;
	}

	public void delete(T data) {
		CompletableFuture.runAsync(() -> {
			try {
				this.storm.delete(data);
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
		});
	}
}