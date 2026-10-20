package dev.pingisbollen.chatnotifier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class NtfyScreen extends Screen {
    private final Screen parent; private EditBox server,topic,token,title,tags,cooldown; private String status="";
    public NtfyScreen(Screen parent){super(Component.literal("ntfy settings"));this.parent=parent;}
    @Override protected void init(){int cx=width/2,y=48; Config c=ChatNotifierClient.CONFIG;
        server=field(cx,y,"Server URL",c.serverUrl); y+=31; topic=field(cx,y,"Topic",c.topic);y+=31;token=field(cx,y,"Bearer token (stored locally)",c.token);token.setValue(c.token); token.addFormatter((value, cursor) -> FormattedCharSequence.forward("•".repeat(value.length()), Style.EMPTY)); y+=31; title=field(cx,y,"Notification title",c.title);y+=31;tags=field(cx,y,"Tags (comma separated)",c.tags);y+=31;cooldown=field(cx,y,"Cooldown seconds",Integer.toString(c.cooldownSeconds));
        addRenderableWidget(server);addRenderableWidget(topic);addRenderableWidget(token);addRenderableWidget(title);addRenderableWidget(tags);addRenderableWidget(cooldown);
        addRenderableWidget(Button.builder(Component.literal("Priority: "+c.priority),b->{c.priority=c.priority%5+1;b.setMessage(Component.literal("Priority: "+c.priority));}).bounds(cx-100,y+29,200,20).build());
        addRenderableWidget(Button.builder(Component.literal("Save"),b->save()).bounds(cx-100,height-50,96,20).build());
        addRenderableWidget(Button.builder(Component.literal("Test"),b->{save();NtfyClient.sendTest();status="Test request queued; check ntfy and the game log.";}).bounds(cx+4,height-50,96,20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"),b->minecraft.gui.setScreen(parent)).bounds(cx-100,height-24,200,20).build());
    }
    private EditBox field(int cx,int y,String hint,String value){EditBox f=new EditBox(font,cx-120,y,240,20,Component.literal(hint));f.setHint(Component.literal(hint));f.setMaxLength(512);f.setValue(value==null?"":value);return f;}
    private void save(){Config c=ChatNotifierClient.CONFIG;try{if(!server.getValue().matches("https?://[^\\s]+"))throw new IllegalArgumentException("Use a valid http:// or https:// server URL.");if(!topic.getValue().matches("[A-Za-z0-9_-]{1,64}"))throw new IllegalArgumentException("Topic must be 1–64 letters, numbers, underscores or hyphens.");c.serverUrl=server.getValue().trim();c.topic=topic.getValue().trim();c.token=token.getValue();c.title=title.getValue().trim();c.tags=tags.getValue().trim();c.cooldownSeconds=Math.max(0,Math.min(86400,Integer.parseInt(cooldown.getValue().trim())));c.save();status="Settings saved.";}catch(Exception e){status=e instanceof NumberFormatException?"Cooldown must be a number.":e.getMessage();}}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float d){super.extractRenderState(g,x,y,d);g.centeredText(font,getTitle(),width/2,18,0xFFFFFF);g.centeredText(font,"Public ntfy topics are not private. Local settings, including tokens, are not encrypted.",width/2,height-72,0xFFCC55);g.centeredText(font,status,width/2,height-62,0xFFFFFF);}
}




