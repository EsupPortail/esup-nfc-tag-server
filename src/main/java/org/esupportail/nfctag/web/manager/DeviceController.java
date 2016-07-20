package org.esupportail.nfctag.web.manager;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.esupportail.nfctag.domain.Device;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/manager/devices")
@Controller
@RooWebScaffold(path = "manager/devices", formBackingObject = Device.class)
public class DeviceController {
	
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Device device, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, device);
            return "manager/devices/update";
        }
        uiModel.asMap().clear();
        Device updateDevice = Device.findDevice(device.getId());
        updateDevice.setValidateAuthWoConfirmation(device.isValidateAuthWoConfirmation());
        updateDevice.merge();
        return "redirect:/manager/devices/";
    }
    
}
