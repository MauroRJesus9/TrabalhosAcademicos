using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Text;
using TPV2.Models;

namespace TPV2.Data
{
    public class ApplicationDbContext : IdentityDbContext<AplicationUser>
    {
        public virtual DbSet<PropertyCategories> PropertyCategories { get; set; }
        public virtual DbSet<Properties> Properties { get; set; }
        public virtual DbSet<Reserves> Reserves { get; set; }
        public virtual DbSet<ReserveStatus> ReserveStatus { get; set; }
        public virtual DbSet<ScoreClient> ScoreClient { get; set; }
        public virtual DbSet<ScoreGestor> ScoreGestor { get; set; }
        public virtual DbSet<AplicationUser> AplicationUser { get; set; }

        public virtual DbSet<PropertiesImages> PropertiesImages { get; set; }


        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
            : base(options)
        {
        }

        protected override void OnModelCreating(ModelBuilder builder)
        {
            base.OnModelCreating(builder);
            

        }

        

    }
}
