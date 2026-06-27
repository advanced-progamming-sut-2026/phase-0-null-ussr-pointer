package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.FireHit;
import com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy;
import com.ussr.pvz.model.entities.projectiles.hit.IceHit;
import com.ussr.pvz.model.entities.projectiles.hit.NormalHit;
import com.ussr.pvz.model.entities.projectiles.move.BounceMove;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.rmi.server.ServerNotActiveException;

public class ShootStrategy implements ActStrategy {

    @Override
    public void act(Plant user, GameSession session) {
        //todo change the json file for shooters to has a vec2 for each projectile they shoot
        if (user.getIntervalTimer() <= 0) {
            String name = user.getName();
            if(name.equalsIgnoreCase("peashooter") || name.equalsIgnoreCase("repeater") ||
            name.equalsIgnoreCase("snow pea") || name.equalsIgnoreCase("citron") ||
            name.equalsIgnoreCase("Fire peashooter") || name.equalsIgnoreCase("goo peashooter") ||
            name.equalsIgnoreCase("mega gatling pea") || name.equalsIgnoreCase("bowling bulb") ||
            name.equalsIgnoreCase("pea pod")) {
                if (straightDetect(user, session)) {
                    HitEffectStrategy hitEffectStrategy;
                    if (user.getTags().contains(Tag.FIRE)) {
                        hitEffectStrategy = new FireHit(1);
                    } else if (user.getTags().contains(Tag.ICE)) {
                        hitEffectStrategy = new IceHit(1);
                    } else
                        hitEffectStrategy = new NormalHit(1);
                    for (int i = 1; i <= user.getAbilityValue(); i++) {
                        session.getProjectiles().add(new Projectile(user.getPosition(), new Vec2(20, 0), user.getDamage(), new StraightMove(), hitEffectStrategy));
                    }
                    user.setInternalTimer(user.getActionInterval());
                }
            }

            else if(name.equalsIgnoreCase("threepeater")) {
                if(threeLineDetect(user , session)) {
                    HitEffectStrategy hitEffectStrategy;
                    if (user.getTags().contains(Tag.FIRE)) {
                        hitEffectStrategy = new FireHit(1);
                    } else if (user.getTags().contains(Tag.ICE)) {
                        hitEffectStrategy = new IceHit(1);
                    } else
                        hitEffectStrategy = new NormalHit(1);
                    for(int i = (int) user.getPosition().x() - 1 ; i < (int) user.getPosition().x() + 1 ; i++) {
                        if(i > 0 && i < 6)
                            session.getProjectiles().add(new Projectile(new Vec2(i , user.getPosition().y()) , new Vec2(0 , 20) , user.getDamage() , new StraightMove() , hitEffectStrategy));
                    }
                    user.setInternalTimer(user.getActionInterval());
                }
            }

            else if(name.equalsIgnoreCase("rotobaga")) {
                if(fourDirectionDetect(user , session)) {
                    HitEffectStrategy hitEffectStrategy;
                    if (user.getTags().contains(Tag.FIRE)) {
                        hitEffectStrategy = new FireHit(1);
                    } else if (user.getTags().contains(Tag.ICE)) {
                        hitEffectStrategy = new IceHit(1);
                    } else
                        hitEffectStrategy = new NormalHit(1);
                    for(int i = -1 ; i < 2 ; i++) {
                        if(i == 0) continue;
                        for (int j = -1 ; j < 2 ; j++) {
                            if(j == 0) continue;
                            session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(i * 20 , j * 20) , user.getDamage(), new StraightMove() , hitEffectStrategy));
                        }
                    }
                    user.setInternalTimer(user.getActionInterval());
                }
            }
            else if(name.equalsIgnoreCase("split pea")) {
                if(straightAndBackDetect(user , session)) {
                    HitEffectStrategy hitEffectStrategy;
                    if (user.getTags().contains(Tag.FIRE)) {
                        hitEffectStrategy = new FireHit(1);
                    } else if (user.getTags().contains(Tag.ICE)) {
                        hitEffectStrategy = new IceHit(1);
                    } else
                        hitEffectStrategy = new NormalHit(1);
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(0 , 20) , user.getDamage(), new StraightMove() , hitEffectStrategy));
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(0 , -20) , user.getDamage(), new StraightMove() , hitEffectStrategy));
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(0 , -20) , user.getDamage(), new StraightMove() , hitEffectStrategy));
                    user.setInternalTimer(user.getActionInterval());
                }
            }
            else if(name.equalsIgnoreCase("starfruit")) {
                if(starDetect(user , session)) {
                    HitEffectStrategy hitEffectStrategy;
                    if (user.getTags().contains(Tag.FIRE)) {
                        hitEffectStrategy = new FireHit(1);
                    } else if (user.getTags().contains(Tag.ICE)) {
                        hitEffectStrategy = new IceHit(1);
                    } else
                        hitEffectStrategy = new NormalHit(1);
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(0 , -20) , user.getDamage(), new StraightMove() , hitEffectStrategy));
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(-20 , 14.53) , user.getDamage(), new StraightMove() , hitEffectStrategy));
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(20 , 14.53) , user.getDamage(), new StraightMove() , hitEffectStrategy));
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(10 , -30.77) , user.getDamage(), new StraightMove() , hitEffectStrategy));
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(-10 , -30.77) , user.getDamage(), new StraightMove() , hitEffectStrategy));
                    user.setInternalTimer(user.getActionInterval());
                }
            }
            else if (name.equalsIgnoreCase("Sea-shroom") || name.equalsIgnoreCase("Puff-shroom")) {
                if(shortRangeStraightDetect(user , session)) {
                    HitEffectStrategy hitEffectStrategy;
                    if (user.getTags().contains(Tag.FIRE)) {
                        hitEffectStrategy = new FireHit(1);
                    } else if (user.getTags().contains(Tag.ICE)) {
                        hitEffectStrategy = new IceHit(1);
                    } else
                        hitEffectStrategy = new NormalHit(1);
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(0 , 20) , user.getDamage() , new StraightMove() , hitEffectStrategy));
                    user.setInternalTimer(user.getActionInterval());
                }
            }
        }
    }

    private boolean straightDetect(Plant user , GameSession session) {
        for (Zombie zombie : session.getZombies()) {
            if(zombie != null) {
                Vec2 itemPos = zombie.getPosition();
                Vec2 userPos = user.getPosition();
                if (itemPos.x() == userPos.x() && itemPos.y() > userPos.y())
                    return true;
            }
        }
        return false;
    }

    private boolean threeLineDetect(Plant user , GameSession session) {
        for (Zombie zombie : session.getZombies()) {
            if(zombie != null) {
                Vec2 itemPos = zombie.getPosition();
                Vec2 userPos = user.getPosition();
                if(Math.abs(itemPos.x() - userPos.x()) <= 1 && itemPos.y() > userPos.y())
                    return true;
            }
        }
        return false;
    }

    private boolean fourDirectionDetect(Plant user , GameSession session) {
        for(Zombie zombie : session.getZombies()) {
            if(zombie != null) {
                Vec2 sub = zombie.getPosition().sub(user.getPosition());
                double slope = sub.x() / sub.y();
                //we can change the accuracy
                if(Math.abs(slope - 1) < 0.95) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean straightAndBackDetect(Plant user , GameSession session) {
        for (Zombie zombie : session.getZombies()) {
            if(zombie != null) {
                Vec2 itemPos = zombie.getPosition();
                Vec2 userPos = user.getPosition();
                if (itemPos.x() == userPos.x())
                    return true;
            }
        }
        return false;
    }

    private boolean starDetect(Plant user , GameSession session) {
        for(Zombie zombie : session.getZombies()) {
            if(zombie != null) {
                Vec2 sub = zombie.getPosition().sub(user.getPosition());
                double slope = sub.y() / sub.x();
                if(Math.abs(slope - 3.077) < 0.1 || Math.abs(slope - 0.726) < 1)
                    return true;
            }
        }
        return false;
    }

    private boolean shortRangeStraightDetect(Plant user , GameSession session) {
        for(Zombie zombie : session.getZombies()) {
            if(zombie != null) {
                Vec2 sub = zombie.getPosition().sub(user.getPosition());
                if(sub.y() >= 0 && sub.y() <= 4)
                    return true;
            }
        }
        return false;
    }
}
