package io.github.tanguygab.realvillagertowns;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ArrowHomingTask extends BukkitRunnable {

    private final Entity arrow;
    private final Entity target;

    public ArrowHomingTask(Entity arrow, Entity target, Plugin plugin) {
        this.arrow = arrow;
        this.target = target;
        runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void run() {
        Vector newVelocity;
        double speed = arrow.getVelocity().length();
        if (arrow.isOnGround() || arrow.isDead() || target.isDead()) {
            cancel();
            return;
        }
        Vector toTarget = target.getLocation().clone().add(new Vector(0.0D, 0.5D, 0.0D)).subtract(arrow.getLocation()).toVector();
        Vector dirVelocity = arrow.getVelocity().clone().normalize();
        Vector dirToTarget = toTarget.clone().normalize();
        double angle = dirVelocity.angle(dirToTarget);
        double newSpeed = 0.9D * speed + 0.14D;
        if (angle >= 0.12D) {
            Vector newDir = dirVelocity.clone().multiply((angle - 0.12D) / angle).add(dirToTarget.clone().multiply(0.12D / angle)).normalize();
            newVelocity = newDir.multiply(newSpeed);
        } else newVelocity = dirVelocity.clone().multiply(newSpeed);
        arrow.setVelocity(newVelocity.add(new Vector(0.0D, 0.03D, 0.0D)));
    }
}
