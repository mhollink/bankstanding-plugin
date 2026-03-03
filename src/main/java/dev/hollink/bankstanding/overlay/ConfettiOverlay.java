package dev.hollink.bankstanding.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ConfettiOverlay extends Overlay
{
	private final Client client;

    private final List<ConfettiParticle> particles = new ArrayList<>();
	private static final Random random = new Random();

	private Instant endTime;

    public void trigger(Duration duration)
    {
		log.debug("Triggering confetti particles for {}s", duration.toSeconds());
        particles.clear();

        for (int i = 0; i < 250; i++)
        {
            ConfettiParticle p = new ConfettiParticle();
            p.x = random.nextInt(client.getCanvasWidth());
            p.y = -random.nextInt(200);
            p.velocityY = 2 + random.nextInt(4);
            p.color = new Color(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
            );
            particles.add(p);
        }

        endTime = Instant.now().plus(duration);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (endTime == null || Instant.now().isAfter(endTime))
        {
            return null;
        }

        for (ConfettiParticle p : particles)
        {
            p.update();
            graphics.setColor(p.color);
            graphics.fillRect(p.x, p.y, 4, 4);
        }

        return null;
    }

	private static class ConfettiParticle
	{
		int x;
		int y;
		int velocityY;
		Color color;

		public void update()
		{
			y += velocityY;
			x += random.nextInt(5) - 2;
		}
	}
}