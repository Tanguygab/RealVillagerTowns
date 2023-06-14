package io.github.tanguygab.realvillagertowns;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

class ArrowHomingTask extends BukkitRunnable {

    private final Arrow arrow;
    private final LivingEntity target;

    public ArrowHomingTask(Arrow arrow, LivingEntity target, Plugin plugin) {
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
        if (angle < 0.12D) newVelocity = dirVelocity.clone().multiply(newSpeed);
        else {
            Vector newDir = dirVelocity.clone().multiply((angle - 0.12D) / angle).add(dirToTarget.clone().multiply(0.12D / angle));
            newDir.normalize();
            newVelocity = newDir.clone().multiply(newSpeed);
        }
        arrow.setVelocity(newVelocity.add(new Vector(0.0D, 0.03D, 0.0D)));
    }
}
