package com.damai.init;

import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.damai.entity.Seat;
import com.damai.entity.TicketCategory;
import com.damai.enums.SellStatus;
import com.damai.mapper.SeatMapper;
import com.damai.mapper.TicketCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class SeatSeeder implements CommandLineRunner {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private TicketCategoryMapper ticketCategoryMapper;

    @Autowired
    private UidGenerator uidGenerator;

    @Override
    public void run(String... args) throws Exception {
        Long programId = 2L;
        System.out.println("Starting SeatSeeder for programId: " + programId);

        // 1. Clear existing seats
        seatMapper.delete(Wrappers.lambdaQuery(Seat.class).eq(Seat::getProgramId, programId));
        System.out.println("Cleared existing seats.");

        // 2. Get ticket categories
        List<TicketCategory> categories = ticketCategoryMapper
                .selectList(Wrappers.lambdaQuery(TicketCategory.class).eq(TicketCategory::getProgramId, programId));

        if (categories.isEmpty()) {
            System.out.println("No ticket categories found for program " + programId);
            return;
        }

        // Sort by price descending (usually higher price = front rows)
        categories.sort(Comparator.comparing(TicketCategory::getPrice).reversed());

        // 3. Generate 500 seats (20 rows x 25 cols)
        int totalRows = 20;
        int cols = 25;
        int currentRow = 1;

        // Distribute rows among categories
        int rowsPerCat = totalRows / categories.size();
        int remainder = totalRows % categories.size();

        for (int i = 0; i < categories.size(); i++) {
            TicketCategory cat = categories.get(i);
            int rowsForThisCat = rowsPerCat + (i < remainder ? 1 : 0);

            for (int r = 0; r < rowsForThisCat; r++) {
                for (int c = 1; c <= cols; c++) {
                    Seat seat = new Seat();
                    seat.setId(uidGenerator.getUid());
                    seat.setProgramId(programId);
                    seat.setTicketCategoryId(cat.getId());
                    seat.setRowCode(currentRow);
                    seat.setColCode(c);
                    seat.setSeatType(1); // Normal seat
                    seat.setPrice(cat.getPrice());
                    seat.setSellStatus(SellStatus.NO_SOLD.getCode());

                    seatMapper.insert(seat);
                }
                currentRow++;
            }
        }

        System.out.println("SeatSeeder completed. Added " + (currentRow - 1) * cols + " seats.");
    }
}
