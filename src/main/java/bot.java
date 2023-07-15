import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.UserData;

import javax.xml.crypto.Data;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class bot {
    private static final Map<Long, Integer> messageCounts = new HashMap<>();

    public static void main(String[] args) {
        String token = "MTEyNDAzMTIwMzY1ODQzMjU3Mw.GOyVc5.axaa3SiYld_3Ot8ROfoPJM1SD87_aUaKmjW23Q"; // замените на токен вашего бота
        Snowflake roleId = Snowflake.of("1123877229819080704"); // замените на ID роли, которую хотите выдать

        DiscordClient client = DiscordClientBuilder.create(token).build();
        GatewayDiscordClient gateway = client.login().block();


        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getContent().equals("!giveRole"))
                .flatMap(event -> {
                    Role role = event.getGuild().block().getRoleById(roleId).block();
                    return event.getMember().get().addRole(role.getId()).then(event.getMessage().getChannel().flatMap(channel -> channel.createMessage("Роль '" +
                            role.getName() + "' успешна выдана пользователю " + event.getMember().get().getDisplayName())));
                })
                .subscribe();


        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getContent().equals("!help"))
                .flatMap(event -> {
                    String helpMessage = "Список доступных команд:\n" +
                            "```!help - показать список команд\n" +
                            "!giveRole - выдать роль 'Боец'\n" +
                            "!info - информация о пользователе\n" +
                            "!roll - бросить кубик и получить случайное число от 1 до 6\n" +
                            "!ping - бот отвечает 'Pong'```";
                    return event.getMessage().getChannel().flatMap(channel -> channel.createMessage(helpMessage));
                })
                .subscribe();

        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getContent().equals("!ping"))
                .flatMap((event -> {
                    String replyMessage = "Pong";
                    return event.getMessage().getChannel().flatMap(channel -> channel.createMessage((replyMessage)));
                })).subscribe();

        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getContent().equals("!roll"))
                .flatMap(event -> {
                    Random rand = new Random();
                    int randomNumber = rand.nextInt(1, 6);
                    event.getMessage().getChannel().flatMap(channel -> channel.createMessage("Бросок кубика...")).subscribe();

                    return event.getMessage().getChannel().flatMap(channel -> channel.createMessage(("Выпало число " + randomNumber)));
                }).subscribe();

        gateway.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
            String messageContent = event.getMessage().getContent();

            if (messageContent.equalsIgnoreCase("!info")) {
                User user = event.getMessage().getAuthor().orElse(null);
                if (user != null) {
                    String userName = String.valueOf(user.getGlobalName());
                    String discriminator = user.getUsername();
                    Instant userData = user.getId().getTimestamp();

                    String userProfile = "```Имя пользователя: " + userName + "\n#" + discriminator + "\nДата регистрации: " + userData
                            + "\n\nС уважением, RBOT```";

                    MessageChannel channel = event.getMessage().getChannel().block();

                    if (channel != null) {
                        channel.createMessage(userProfile).block();
                    }
                }
            }
        });

        gateway.onDisconnect().block();
    }
}
