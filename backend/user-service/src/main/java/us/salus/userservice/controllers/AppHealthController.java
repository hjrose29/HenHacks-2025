package us.salus.userservice.controllers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/write")
public class AppHealthController {

    /**
     * Basic health check endpoint that returns current system timestamp.
     *
     * @return ResponseEntity containing current timestamp in milliseconds
     */

    @GetMapping("/health")
    public ResponseEntity<Long> healthCheck() {
        return ResponseEntity.ok(System.currentTimeMillis());
    }

    /**
     * Test endpoint that creates and returns a person's address.
     *
     * @return ResponseEntity containing a test address string
     */
}
