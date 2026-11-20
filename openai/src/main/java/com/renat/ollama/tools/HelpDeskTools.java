package com.renat.ollama.tools;

import com.renat.ollama.entity.HelpDeskTicket;
import com.renat.ollama.model.TicketRequest;
import com.renat.ollama.service.HelpDeskTicketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HelpDeskTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpDeskTools.class);

    private final HelpDeskTicketService service;

    @Tool(name = "createTicket", description = "Create the Support Ticket", returnDirect = true)
    String createTicket(@ToolParam(description = "Details to create a Support ticket") TicketRequest ticketRequest,
                        ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        LOGGER.info("Creating support ticket for user: {} with details: {}", username, ticketRequest);
        HelpDeskTicket savedTicket = service.createTicket(ticketRequest, username);
        LOGGER.info("Ticker created successfully. Ticket ID: {}, Username: {}", savedTicket.getId(), username);
        return "Ticket #" + savedTicket.getId() + " created successfully for user " + savedTicket.getUsername();
    }

    @Tool(description = "fetch the status of the tickets based on a given username")
    List<HelpDeskTicket> getTicketStatus(ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        LOGGER.info("Fetching open tickets for user: {}", username);
        List<HelpDeskTicket> tickets =  service.getTicketByUserName(username);
        LOGGER.info("Found {} open tickets for user {}", tickets.size(), username);
        return tickets;
    }
}
