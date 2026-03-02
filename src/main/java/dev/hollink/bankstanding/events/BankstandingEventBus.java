package dev.hollink.bankstanding.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BankstandingEventBus
{
	private final List<Consumer<BankstandingEvent>> listeners = new ArrayList<>();

	public void register(Consumer<BankstandingEvent> listener)
	{
		log.trace("Registering listener: {}", listener);
		listeners.add(listener);
	}

	public void unregister(Consumer<BankstandingEvent> listener)
	{
		log.trace("Removing listener: {}", listener);
		listeners.remove(listener);
	}

	public void publish(BankstandingEvent event)
	{
		log.debug("Publishing event: {}", event.getClass().getSimpleName());
		for (Consumer<BankstandingEvent> listener : List.copyOf(listeners))
		{
			listener.accept(event);
		}
	}
}
