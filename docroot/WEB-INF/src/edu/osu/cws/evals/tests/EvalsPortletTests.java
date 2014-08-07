package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.portlet.EvalsPortlet;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.portlet.*;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

@Test
public class EvalsPortletTests {

    private EvalsPortlet mockedEvalsPortlet;
    private ResourceRequest mockedResourceRequest;
    private ResourceResponse mockedResourceResponse;
    private PortletContext mockedPortletContext;
    private PortletSession mockedPortletSession;


    @BeforeMethod
    public void setup() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();

        mockedEvalsPortlet = spy(new EvalsPortlet());
        mockedEvalsPortlet.setValidateSession(false);
        mockedResourceRequest = mock(ResourceRequest.class);
        mockedResourceResponse = mock(ResourceResponse.class);
        mockedPortletSession = mock(PortletSession.class);
        mockedPortletContext = mock(PortletContext.class);
        doReturn(mockedPortletContext).when(mockedEvalsPortlet).getPortletContext();
        when(mockedResourceRequest.isRequestedSessionIdValid()).thenReturn(true);
        when(mockedResourceRequest.getPortletSession(true)).thenReturn(mockedPortletSession);
        when(mockedResourceResponse.getWriter()).thenReturn(new PrintWriter("test"));
    }

    public void shouldSetUsernameInServeResource() throws Exception {
        String username = "barlowc";
        when(mockedResourceRequest.getResourceID()).thenReturn("testing");
        when(mockedPortletSession.getAttribute("onidUsername")).thenReturn(username);

        mockedEvalsPortlet.serveResource(mockedResourceRequest, mockedResourceResponse);
        verify(mockedResourceRequest).getResourceID();
        verify(mockedPortletSession).getAttribute("loggedOnUser");
        assert mockedEvalsPortlet.getActionHelper().getLoggedOnUser().getOnid().equals(username);
    }
}
