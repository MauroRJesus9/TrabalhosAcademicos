using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using TPV2.Data;
using TPV2.Models;

namespace TPV2.Controllers
{
    [Authorize(Roles = "Admin")]
    public class ReserveStatusController : Controller
    {
        private readonly ApplicationDbContext _context;

        public ReserveStatusController(ApplicationDbContext context)
        {
            _context = context;
        }

        // GET: ReserveStatus
        public async Task<IActionResult> Index()
        {
            return View(await _context.ReserveStatus.Where(r => r.isRemoved != true).ToListAsync());
        }

        // GET: ReserveStatus/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var reserveStatus = await _context.ReserveStatus
                .FirstOrDefaultAsync(m => m.StatusId == id);
            if (reserveStatus == null)
            {
                return NotFound();
            }

            return View(reserveStatus);
        }

        // GET: ReserveStatus/Create
        public IActionResult Create()
        {
            return View();
        }

        // POST: ReserveStatus/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("StatusId,Description")] ReserveStatus reserveStatus)
        {
            reserveStatus.isRemoved = false;
            if (ModelState.IsValid)
            {
                _context.Add(reserveStatus);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            return View(reserveStatus);
        }

        // GET: ReserveStatus/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var reserveStatus = await _context.ReserveStatus.FindAsync(id);
            if (reserveStatus == null)
            {
                return NotFound();
            }
            return View(reserveStatus);
        }

        // POST: ReserveStatus/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("StatusId,Description,isRemoved")] ReserveStatus reserveStatus)
        {
            if (id != reserveStatus.StatusId)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(reserveStatus);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!ReserveStatusExists(reserveStatus.StatusId))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            return View(reserveStatus);
        }

        // GET: ReserveStatus/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var reserveStatus = await _context.ReserveStatus
                .FirstOrDefaultAsync(m => m.StatusId == id);
            if (reserveStatus == null)
            {
                return NotFound();
            }

            return View(reserveStatus);
        }

        // POST: ReserveStatus/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var reserveStatus = await _context.ReserveStatus.FindAsync(id);
            _context.ReserveStatus.Remove(reserveStatus);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool ReserveStatusExists(int id)
        {
            return _context.ReserveStatus.Any(e => e.StatusId == id);
        }
    }
}
