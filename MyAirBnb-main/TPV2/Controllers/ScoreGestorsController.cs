using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using TPV2.Data;
using TPV2.Models;

namespace TPV2.Controllers
{
    [Authorize(Roles = "Admin,Gestor")]
    public class ScoreGestorsController : Controller
    {
        private readonly ApplicationDbContext _context;

        public ScoreGestorsController(ApplicationDbContext context)
        {
            _context = context;
        }

        // GET: ScoreGestors
        public async Task<IActionResult> Index()
        {
            var user = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (User.IsInRole("Gestor"))
            {
                var applicationDbContext = _context.ScoreGestor
                                .Include(s => s.Reserves)
                                .Include(s => s.SGGestorID)
                                .Include(s => s.SGUserID)
                                .Where(s => s.isRemoved != true);
                return View(await applicationDbContext.ToListAsync());
            }
            else
            {
                var applicationDbContext = _context.ScoreGestor.Include(s => s.Reserves).Include(s => s.SGGestorID).Include(s => s.SGUserID);
                return View(await applicationDbContext.ToListAsync());
            }
        }

        // GET: ScoreGestors/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var scoreGestor = await _context.ScoreGestor
                .Include(s => s.Reserves)
                .Include(s => s.SGGestorID)
                .Include(s => s.SGUserID)
                .FirstOrDefaultAsync(m => m.ScoreGestorId == id);
            if (scoreGestor == null)
            {
                return NotFound();
            }

            return View(scoreGestor);
        }

        // GET: ScoreGestors/Create
        public IActionResult Create(int? reserveid, string? clientid)
        {
            //Verificar se já existe alguma avaliação com este id de reserva
            var pesquisa_id = (from reserva in _context.ScoreGestor
                               where reserva.ReserveId == reserveid
                               select reserva);

            if (pesquisa_id.Any())
            { //Já existe uma avaliação com o mesmo id de reserva

                return Redirect("/Reserves");
            }
            else
            {
                if (reserveid != null && clientid != null)
                {
                    ViewData["ReserveId"] = new SelectList(_context.Reserves.Where(r => r.ReserveId == reserveid), "ReserveId", "ReserveId");
                    ViewData["ClientId"] = new SelectList(_context.AplicationUser.Where(c => c.Id == clientid), "Id", "UserName");
                }
                else
                {
                    ViewData["ReserveId"] = new SelectList(_context.Reserves, "ReserveId", "ReserveId");
                    ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "UserName");
                }

                ViewData["GestorId"] = new SelectList(_context.AplicationUser, "Id", "UserName");
                return View();
            }
        }

        // POST: ScoreGestors/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("ReserveId,Comments,ScoreClient,ClientId")] ScoreGestor scoreGestor)
        {
            scoreGestor.isRemoved = false;
            scoreGestor.GestorId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (ModelState.IsValid)
            {
                _context.Add(scoreGestor);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            ViewData["ReserveId"] = new SelectList(_context.Reserves, "ReserveId", "ReserveId", scoreGestor.ReserveId);
            ViewData["GestorId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreGestor.GestorId);
            ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreGestor.ClientId);
            return View(scoreGestor);
        }

        // GET: ScoreGestors/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var scoreGestor = await _context.ScoreGestor.FindAsync(id);
            if (scoreGestor == null)
            {
                return NotFound();
            }
            ViewData["ReserveId"] = new SelectList(_context.Reserves, "ReserveId", "ReserveId", scoreGestor.ReserveId);
            ViewData["GestorId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreGestor.GestorId);
            ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreGestor.ClientId);
            return View(scoreGestor);
        }

        // POST: ScoreGestors/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("ReserveId,Comments,ScoreClient,ClientId,isRemoved")] ScoreGestor scoreGestor)
        {
            if (id != scoreGestor.ScoreGestorId)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(scoreGestor);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!ScoreGestorExists(scoreGestor.ScoreGestorId))
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
            ViewData["ReserveId"] = new SelectList(_context.Reserves, "ReserveId", "ReserveId", scoreGestor.ReserveId);
            ViewData["GestorId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreGestor.GestorId);
            ViewData["ClientId"] = new SelectList(_context.AplicationUser, "Id", "UserName", scoreGestor.ClientId);
            return View(scoreGestor);
        }

        // GET: ScoreGestors/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var scoreGestor = await _context.ScoreGestor
                .Include(s => s.Reserves)
                .Include(s => s.SGGestorID)
                .Include(s => s.SGUserID)
                .FirstOrDefaultAsync(m => m.ScoreGestorId == id);
            if (scoreGestor == null)
            {
                return NotFound();
            }

            return View(scoreGestor);
        }

        // POST: ScoreGestors/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var scoreGestor = await _context.ScoreGestor.FindAsync(id);
            _context.ScoreGestor.Remove(scoreGestor);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool ScoreGestorExists(int id)
        {
            return _context.ScoreGestor.Any(e => e.ScoreGestorId == id);
        }
    }
}
