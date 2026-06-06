package com.authora.authorization.server.authentication.web;

import com.authora.authorization.server.authentication.dto.SignUpRequest;
import com.authora.authorization.server.authentication.service.SignUpService;
import com.authora.authorization.server.client.mapper.RegisteredClientMapper;
import com.authora.authorization.server.client.model.RegisteredClientModel;
import com.authora.authorization.server.connection.mapper.AppConnectionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SignUpController {
    private final SignUpService signUpService;
    private final RegisteredClientMapper registeredClientMapper;
    private final AppConnectionMapper appConnectionMapper;

    @GetMapping("/sign-up")
    public String signUpPage(@RequestParam(value = "client_id",required = false) String clientId, Model model){
        model.addAttribute("clientId", clientId);
        if (clientId!=null && !clientId.equals("authora-dashboard") && !clientId.isBlank()){
            System.out.println(clientId);
            RegisteredClientModel clientModel = registeredClientMapper.findByClientId(clientId)
                    .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Invalid Client Id"));
            model.addAttribute("isB2C",true);
            model.addAttribute("appName",clientModel.getClientName());
            model.addAttribute("connections", appConnectionMapper.findEnabledOptionsByClientId(clientId));
        }else{
            model.addAttribute("isB2C",false);
            model.addAttribute("appName", "");
            model.addAttribute("connections", List.of());
        }
        return "auth/sign-up/index";
    }

    @PostMapping("/sign-up")
    public String signUpPage(@Valid @ModelAttribute SignUpRequest request, BindingResult bindingResult, Model model){
        model.addAttribute("email", request.getEmail());
        model.addAttribute("clientId", request.getClientId());
        if (bindingResult.hasErrors()){
            model.addAttribute("errors", bindingResult);
            boolean isB2C = request.getClientId() != null && !request.getClientId().equals("authora-dashboard") && !request.getClientId().isBlank();
            model.addAttribute("isB2C", isB2C);
            if (isB2C) {
                RegisteredClientModel client = registeredClientMapper.findByClientId(request.getClientId()).orElse(null);
                model.addAttribute("appName", client != null ? client.getClientName() : "Authora");
                model.addAttribute("connections", appConnectionMapper.findEnabledOptionsByClientId(request.getClientId()));
            } else {
                model.addAttribute("connections", List.of());
            }
            return "auth/sign-up/index";
        }
        signUpService.signup(request);
        if (request.getClientId() != null && !request.getClientId().isBlank()) {
            return "redirect:/sign-in?registered=true&client_id=" + request.getClientId();
        }
        return "redirect:/sign-in?registered=true";
    }


}
